package com.baulsupp.okurl.services.basicauth

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.lyft.com/docs/authentication
 */
class BasicAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("basic", "Basic Auth", "basic",
    "https://en.wikipedia.org/wiki/Basic_access_authentication")

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder().addHeader("Authorization", "GenieKey ${credentials.accessToken}").build()

    return chain.proceed(request)
  }

  override fun supportsUrl(url: HttpUrl): Boolean {
    return url.encodedPath().startsWith("/hidden-basic-auth/") || url.encodedPath().startsWith("/basic-auth/")
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val apiKey = Secrets.prompt("OpsGenie API Key", "opsgenie.apiKey", "", false)

    return Oauth2Token(apiKey)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.lyft.com/v1/profile",
      TokenValue(credentials), "id"))

  override fun canRenew(credentials: Oauth2Token): Boolean = false

  override fun hosts(): Set<String> = setOf("api.opsgenie.com")
}
