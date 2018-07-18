package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.google.firebase.FirebaseCompleter
import com.baulsupp.okurl.services.google.model.AuthError
import com.baulsupp.okurl.util.ClientException
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

  override val serviceDefinition = Oauth2ServiceDefinition("www.googleapis.com", "Google API", "google",
    "https://developers.google.com/",
    "https://console.developers.google.com/apis/credentials")

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()

    val response = chain.proceed(request)

    if (isFirebaseHost(request.url().host())) {
      if (response.isSuccessful) {
        FirebaseCompleter.registerKnownHost(request.url().host())
      }
    }

    return response
  }

  override fun supportsUrl(url: HttpUrl): Boolean {
    val host = url.host()

    return setOf((
      "api.google.com")
    ).contains(host) || host.endsWith(".googleapis.com") || host.endsWith(".firebaseio.com")
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Google Client Id", "google.clientId", "", false)
    val clientSecret = Secrets.prompt("Google Client Secret", "google.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "google.scopes", listOf("plus.login", "plus.profile.emails.read"))

    return GoogleAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://www.googleapis.com/oauth2/v3/userinfo",
      TokenValue(credentials), "name"))

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

    val responseMap = client.queryMap<Any>(request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret)
  }

  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter =
    if (!isPastHost(prefix)) {
      hostCompletion(completionVariableCache)
    } else if (isFirebaseUrl(prefix)) {
      FirebaseCompleter(client)
    } else {
      discoveryCompletion(prefix, client)
    }

  private fun isFirebaseUrl(url: String): Boolean = HttpUrl.parse(url)?.host()?.let { isFirebaseHost(it) } ?: false

  private fun isFirebaseHost(host: String) = host.endsWith(".firebaseio.com")

  fun discoveryCompletion(prefix: String, client: OkHttpClient): GoogleDiscoveryCompleter {
    val discoveryPaths = DiscoveryIndex.instance.getDiscoveryUrlForPrefix(prefix)

    return GoogleDiscoveryCompleter.forApis(DiscoveryRegistry(client), discoveryPaths)
  }

  fun googleDiscoveryHosts(): Set<String> {
    return UrlList.fromResource(name())!!.getUrls("").map {
      HttpUrl.parse(it)!!.host()
    }.toSet()
  }

  fun hostCompletion(completionVariableCache: CompletionVariableCache) =
    BaseUrlCompleter(UrlList.fromResource(name())!!, hosts() + firebaseHosts(), completionVariableCache)

  private fun firebaseHosts() = FirebaseCompleter.knownHosts()

  override fun hosts(): Set<String> = foundHosts

  private fun isPastHost(prefix: String): Boolean = prefix.matches("https://.*/.*".toRegex())

  override fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter = DiscoveryApiDocPresenter(DiscoveryRegistry(client))

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
