package com.baulsupp.okurl.services.giphy

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class GiphyAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "api.giphy.com", "Giphy API", "giphy",
      "https://github.com/Giphy/GiphyAPI", null
    )

  override fun defaultCredentials(): Oauth2Token? = Oauth2Token("dc6zaTOxFJmzC")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url.newBuilder().addQueryParameter("api_key", token).build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val apiKey = Secrets.prompt("Giphy API Key", "giphy.apiKey", "", false)

    return Oauth2Token(apiKey)
  }
}
