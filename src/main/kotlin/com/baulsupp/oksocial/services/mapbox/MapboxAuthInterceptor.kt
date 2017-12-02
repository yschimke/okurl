package com.baulsupp.oksocial.services.mapbox

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class MapboxAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition =
          Oauth2ServiceDefinition("api.mapbox.com", "Mapbox API", "mapbox",
                  "https://www.mapbox.com/api-documentation/",
                  "https://www.mapbox.com/studio/account/tokens/")

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Mapbox API")

    val apiKey = Secrets.prompt("Mapbox Access Token", "mapbox.accessToken", "", false)

    return Oauth2Token(apiKey)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials("?", null)

  override fun defaultCredentials(): Oauth2Token? = Oauth2Token(
          "pk.eyJ1IjoieXNjaGlta2UiLCJhIjoiY2l0eGRkc245MDAzODJ5cDF2Z3l2czJjaSJ9.9XMBjr0vkbh2WD74DQcd3w")

  override fun hosts(): Set<String> = setOf((
          "api.mapbox.com")
  )
}
