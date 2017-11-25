package com.baulsupp.oksocial.services.giphy

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class GiphyAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition =
          Oauth2ServiceDefinition("api.giphy.com", "Giphy API", "giphy",
              "https://github.com/Giphy/GiphyAPI", null)

  override fun defaultCredentials(): Oauth2Token? = Oauth2Token("dc6zaTOxFJmzC")

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("api_key", token).build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                         authArguments: List<String>): Oauth2Token {
    outputHandler.showError("Authorising Giphy API", null)

    val apiKey = Secrets.prompt("Giphy API Key", "giphy.apiKey", "", false)

    return Oauth2Token(apiKey)
  }

  override suspend fun validate(client: OkHttpClient,
                                requestBuilder: Request.Builder, credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials("?", null)

  override fun hosts(): Set<String> = GiphyUtil.API_HOSTS
}
