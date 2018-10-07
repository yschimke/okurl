package com.baulsupp.okurl.services.mapbox

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class MapboxAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition =
    Oauth2ServiceDefinition("api.mapbox.com", "Mapbox API", "mapbox",
      "https://www.mapbox.com/api-documentation/",
      "https://www.mapbox.com/studio/account/tokens/")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val apiKey = Secrets.prompt("Mapbox Access Token", "mapbox.accessToken", "", false)

    return Oauth2Token(apiKey)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials("✓", null)

  override fun defaultCredentials(): Oauth2Token? = Oauth2Token(
    "pk.eyJ1IjoieXNjaGlta2UiLCJhIjoiY2plbW82ZDRmMHFjYTJxczMwbjZyb283biJ9.kR_CuRmA-qdRAU0rAlzN_Q")

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf((
    "api.mapbox.com")
  )
}
