package com.baulsupp.oksocial.services.github

import com.baulsupp.oksocial.TokenValue
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.github.com/docs/authentication
 */
class GithubAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.github.com", "Github API", "github",
      "https://developer.github.com/v3/", "https://github.com/settings/developers")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "token $token").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Github Client Id", "github.clientId", "", false)
    val clientSecret = Secrets.prompt("Github Client Secret", "github.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "github.scopes",
      listOf("user", "repo", "gist", "admin:org"))

    return GithubAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.github.com/user", TokenValue(credentials), "name"))

  override fun hosts(): Set<String> = setOf("api.github.com", "uploads.github.com")
}
