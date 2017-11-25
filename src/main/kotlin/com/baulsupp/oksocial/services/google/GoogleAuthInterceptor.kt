package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * https://developer.google.com/docs/authentication
 */
class GoogleAuthInterceptor : AuthInterceptor<Oauth2Token> {
  private val foundHosts by lazy {
    UrlList.fromResource(name())!!.getUrls("").map { HttpUrl.parse(it)!!.host() }.toSet()
  }
  private val discoveryIndex by lazy { DiscoveryIndex.loadStatic() }

  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("www.googleapis.com", "Google API", "google",
        "https://developers.google.com/", "https://console.developers.google.com/apis/credentials")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  override fun supportsUrl(url: HttpUrl): Boolean {
    val host = url.host()

    return GoogleUtil.API_HOSTS.contains(host) || host.endsWith(".googleapis.com")
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                         authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Google API")

    val clientId = Secrets.prompt("Google Client Id", "google.clientId", "", false)
    val clientSecret = Secrets.prompt("Google Client Secret", "google.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "google.scopes", GoogleUtil.SCOPES)

    return GoogleAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                requestBuilder: Request.Builder, credentials: Oauth2Token): ValidatedCredentials {
    return JsonCredentialsValidator(
        requestBuilder.url("https://www.googleapis.com/oauth2/v3/userinfo").build(),
        { it["name"] as String }).validate(client)
  }

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
                            credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter =
      if (isPastHost(prefix)) {
        val discoveryPaths = DiscoveryIndex.loadStatic().getDiscoveryUrlForPrefix(prefix)

        GoogleDiscoveryCompleter.forApis(DiscoveryRegistry.instance(client),
            discoveryPaths)
      } else {
        BaseUrlCompleter(UrlList.fromResource(name())!!, hosts())
      }

  override fun hosts(): Set<String> = foundHosts

  private fun isPastHost(prefix: String): Boolean = prefix.matches("https://.*/.*".toRegex())

  @Throws(IOException::class)
  override fun apiDocPresenter(url: String): ApiDocPresenter = DiscoveryApiDocPresenter(discoveryIndex)
}
