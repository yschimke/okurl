package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.google.firebase.FirebaseCompleter
import com.baulsupp.oksocial.services.google.model.AuthError
import com.baulsupp.oksocial.util.ClientException
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * https://developer.google.com/docs/authentication
 */
class GoogleAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  private val foundHosts by lazy {
    googleDiscoveryHosts()
  }

  private val discoveryIndex by lazy { DiscoveryIndex.loadStatic() }
  private val firebaseCompleter = FirebaseCompleter()

  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("www.googleapis.com", "Google API", "google",
            "https://developers.google.com/",
            "https://console.developers.google.com/apis/credentials")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  override fun supportsUrl(url: HttpUrl): Boolean {
    val host = url.host()

    return GoogleUtil.API_HOSTS.contains(host) || host.endsWith(".googleapis.com") || host.endsWith(".firebaseio.com")
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Google Client Id", "google.clientId", "", false)
    val clientSecret = Secrets.prompt("Google Client Secret", "google.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "google.scopes", GoogleUtil.SCOPES)

    return GoogleAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials(client.queryMapValue<String>("https://www.googleapis.com/oauth2/v3/userinfo", "name"))

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val body = FormBody.Builder().add("client_id", credentials.clientId!!)
            .add("refresh_token", credentials.refreshToken!!)
            .add("client_secret", credentials.clientSecret!!)
            .add("grant_type", "refresh_token")
            .build()

    val request = Request.Builder().url("https://www.googleapis.com/oauth2/v4/token")
            .post(body)
            .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
            credentials.refreshToken!!, credentials.clientId!!,
            credentials.clientSecret!!)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter =
          if (!isPastHost(prefix)) {
            hostCompletion(completionVariableCache)
          } else if (prefix.contains(".firebaseio.com/")) {
            firebaseCompleter
          } else {
            discoveryCompletion(prefix, client)
          }

  fun discoveryCompletion(prefix: String, client: OkHttpClient): GoogleDiscoveryCompleter {
    val discoveryPaths = DiscoveryIndex.loadStatic().getDiscoveryUrlForPrefix(prefix)

    return GoogleDiscoveryCompleter.forApis(DiscoveryRegistry.instance(client),
            discoveryPaths)
  }

  fun googleDiscoveryHosts() =
          UrlList.fromResource(name())!!.getUrls("").map { HttpUrl.parse(it)!!.host() }.toSet()

  fun hostCompletion(completionVariableCache: CompletionVariableCache) =
          BaseUrlCompleter(UrlList.fromResource(name())!!, hosts() + firebaseHosts(), completionVariableCache)

  private fun firebaseHosts() = firebaseCompleter.hosts()

  override fun hosts(): Set<String> = foundHosts

  private fun isPastHost(prefix: String): Boolean = prefix.matches("https://.*/.*".toRegex())

  override fun apiDocPresenter(url: String): ApiDocPresenter = DiscoveryApiDocPresenter(
          discoveryIndex)


  override fun errorMessage(ce: ClientException): String {
    if (ce.code == 401) {
      try {
        val message = ce.responseMessage

        return moshi.adapter(AuthError::class.java).fromJson(message)!!.error_description
      } catch (e: Exception) {
        // ignore
      }
    }

    return super.errorMessage(ce)
  }
}
