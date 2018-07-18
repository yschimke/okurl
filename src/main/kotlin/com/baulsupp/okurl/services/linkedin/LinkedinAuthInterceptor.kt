package com.baulsupp.okurl.services.linkedin

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class LinkedinAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("api.linkedin.com", "Linkedin API", "linkedin",
      "https://developer.linkedin.com/docs/rest-api",
      "https://www.linkedin.com/developer/apps")

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val request = chain.request()

    val token = credentials.accessToken

    val requestBuilder = request.newBuilder().addHeader("Authorization", "Bearer $token")

    if (request.url().queryParameter("format") == null && request.header("x-li-format") == null) {
      requestBuilder.addHeader("x-li-format", "json")
    }

    return chain.proceed(requestBuilder.build())
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Linkedin Client Id", "linkedin.clientId", "", false)
    val clientSecret = Secrets.prompt("Linkedin Client Secret", "linkedin.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "linkedin.scopes",
      listOf("r_basicprofile", "r_emailaddress", "rw_company_admin", "w_share"))

    return LinkedinAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    return ValidatedCredentials(client.queryMapValue<String>("https://api.linkedin.com/v1/people/~:(formatted-name)",
      TokenValue(credentials), "formattedName"))
  }

  override fun hosts(): Set<String> {
    return setOf((
      "api.linkedin.com"))
  }
}
