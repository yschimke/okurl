package com.baulsupp.oksocial.services.instagram

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class InstagramAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.instagram.com", "Instagram API", "instagram",
      "https://www.instagram.com/developer/endpoints/",
      "https://www.instagram.com/developer/clients/manage/")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Instagram Client Id", "instagram.clientId", "", false)
    val clientSecret = Secrets.prompt("Instagram Client Secret", "instagram.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "instagram.scopes",
      listOf("basic", "public_content", "follower_list", "comments", "relationships",
        "likes"))

    return InstagramAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.instagram.com/v1/users/self",
      TokenValue(credentials), "data", "full_name"))

  override fun hosts(): Set<String> = setOf("api.instagram.com")
}
