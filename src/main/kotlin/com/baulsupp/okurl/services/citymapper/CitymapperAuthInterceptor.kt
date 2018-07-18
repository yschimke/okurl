package com.baulsupp.okurl.services.citymapper

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.lyft.com/docs/authentication
 */
class CitymapperAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("developer.citymapper.com", "Citymapper API", "citymapper",
    "https://citymapper.3scale.net/docs", "https://citymapper.3scale.net/")

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("key", token).build()

    val builder = request.newBuilder()
    request = builder.url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token =
    Oauth2Token(Secrets.prompt("Citymapper Key", "citymapper.token", "", true))

  override fun hosts(): Set<String> = setOf("developer.citymapper.com")
}
