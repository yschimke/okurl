package com.baulsupp.oksocial.services.linkedin

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
import java.io.IOException
import java.util.Arrays

class LinkedinAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.linkedin.com", "Linkedin API", "linkedin",
      "https://developer.linkedin.com/docs/rest-api",
      "https://www.linkedin.com/developer/apps")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val request = chain.request()

    val token = credentials.accessToken

    val requestBuilder = request.newBuilder().addHeader("Authorization", "Bearer " + token)

    if (request.url().queryParameter("format") == null && request.header("x-li-format") == null) {
      requestBuilder.addHeader("x-li-format", "json")
    }

    return chain.proceed(requestBuilder.build())
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Linkedin Client Id", "linkedin.clientId", "", false)
    val clientSecret = Secrets.prompt("Linkedin Client Secret", "linkedin.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "linkedin.scopes",
      Arrays.asList("r_basicprofile", "r_emailaddress", "rw_company_admin", "w_share"))

    return LinkedinAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    return ValidatedCredentials(client.queryMapValue<String>("https://api.linkedin.com/v1/people/~:(formatted-name)", "formattedName"))
  }

  override fun hosts(): Set<String> {
    return setOf((
      "api.linkedin.com"))
  }
}
