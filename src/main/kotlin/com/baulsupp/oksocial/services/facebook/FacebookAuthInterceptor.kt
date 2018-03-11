package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.facebook.FacebookUtil.ALL_PERMISSIONS
import com.baulsupp.oksocial.services.facebook.model.App
import com.baulsupp.oksocial.services.facebook.model.UserOrPage
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.ByteString

class FacebookAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("graph.facebook.com", "Facebook API", "facebook",
      "https://developers.facebook.com/docs/graph-api",
      "https://developers.facebook.com/apps/")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    if (isGraphApi(request.url()) || isScimApi(request.url())) {
      val token = credentials.accessToken

      val builder = request.url().newBuilder().addQueryParameter("access_token", token)

      if (credentials.clientSecret != null) {
        val appsecretTime = (System.currentTimeMillis() / 1000).toString()
        val appsecretProof = ByteString.encodeUtf8("$token|$appsecretTime").hmacSha256(
          ByteString.encodeUtf8(credentials.clientSecret)).hex()
        builder.addQueryParameter("appsecret_proof", appsecretProof)
        builder.addQueryParameter("appsecret_time", appsecretTime)
      }

      val newUrl = builder.build()

      request = request.newBuilder().url(newUrl).build()
    }

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Facebook App Id", "facebook.appId", "", false)
    val clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", "", true)
    var scopes = Secrets.promptArray("Scopes", "facebook.scopes",
      listOf("public_profile", "user_friends", "email"))

    if (scopes.contains("all")) {
      scopes = ALL_PERMISSIONS
    }

    return FacebookAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    val userName = client.fbQuery<UserOrPage>("/me",
      TokenValue(credentials)).name
    val appName = client.fbQuery<App>("/app",
      TokenValue(credentials)).name

    return ValidatedCredentials(userName, appName)
  }

  override fun hosts(): Set<String> = FacebookUtil.API_HOSTS

  override fun supportsUrl(url: HttpUrl): Boolean = isGraphApi(url) || isScimApi(url)

  fun isScimApi(url: HttpUrl) =
    url.host().startsWith("www.") && url.host().endsWith(".facebook.com") && url.encodedPath().contains("/scim/v1/")

  fun isGraphApi(url: HttpUrl) =
    url.host().startsWith("graph.") && url.host().endsWith(".facebook.com")

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: Token): ApiCompleter =
    FacebookCompleter(client, hosts())

  override fun apiDocPresenter(url: String): ApiDocPresenter = FacebookApiDocPresenter(serviceDefinition())
}
