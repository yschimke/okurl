package com.baulsupp.okurl.services.facebook

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.facebook.FacebookAuthFlow.ALL_PERMISSIONS
import com.baulsupp.okurl.services.facebook.model.App
import com.baulsupp.okurl.services.facebook.model.UserOrPage
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.ByteString.Companion.encodeUtf8

class FacebookAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("graph.facebook.com", "Facebook API", "facebook",
    "https://developers.facebook.com/docs/graph-api",
    "https://developers.facebook.com/apps/")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val request = chain.request()

    if (supportsUrl(request.url()) && !authenticated(request)) {
      val builder = request.url().newBuilder().addQueryParameter("access_token", credentials.accessToken)

      if (credentials.clientSecret != null) {
        val appsecretTime = (System.currentTimeMillis() / 1000).toString()
        val appsecretProof = "${credentials.accessToken}|$appsecretTime".encodeUtf8().hmacSha256(
          credentials.clientSecret.encodeUtf8()).hex()
        builder.addQueryParameter("appsecret_proof", appsecretProof)
        builder.addQueryParameter("appsecret_time", appsecretTime)
      }

      val signedRequest = request.newBuilder().url(builder.build()).build()

      return chain.proceed(signedRequest)
    }

    return chain.proceed(request)
  }

  fun authenticated(request: Request): Boolean {
    return request.url().queryParameter("access_token") != null || request.header("Authorization") != null
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    if (authArguments == listOf("--ci")) {
      val clientId = Secrets.prompt("Workplace CI App Id", "workplace.ci.appId", "", false)
      val clientSecret = Secrets.prompt("Workplace CI App Secret", "workplace.ci.appSecret", "", true)
      val token = Secrets.prompt("Workplace CI Token", "workplace.ci.token", "", true)

      return Oauth2Token(token, "ci", clientId, clientSecret)
    } else {
      val clientId = Secrets.prompt("Facebook App Id", "facebook.appId", "", false)
      val clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", "", true)
      var scopes = Secrets.promptArray("Scopes", "facebook.scopes",
        listOf("public_profile", "user_friends", "email"))

      if (scopes.contains("all")) {
        scopes = ALL_PERMISSIONS
      }
      return FacebookAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val userName = client.fbQuery<UserOrPage>("/me",
      TokenValue(credentials)).name
    val appName = client.fbQuery<App>("/app",
      TokenValue(credentials)).name

    return ValidatedCredentials(userName, appName)
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = API_HOSTS

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore) = supportsUrl(url)

  fun supportsUrl(url: HttpUrl): Boolean = isGraphApi(url) || isScimApi(url) || isStreamingGraphApi(url)

  fun isScimApi(url: HttpUrl) =
    url.host().startsWith("www.") && url.host().endsWith(".facebook.com") && url.encodedPath().contains("/scim/v1/")

  fun isGraphApi(url: HttpUrl) =
    url.host().startsWith("graph.") && url.host().endsWith(".facebook.com")

  fun isStreamingGraphApi(url: HttpUrl) =
    url.host().startsWith("streaming-graph.") && url.host().endsWith(".facebook.com")

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter =
    FacebookCompleter(client, hosts(credentialsStore))

  override fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter = FacebookApiDocPresenter(serviceDefinition)
}
