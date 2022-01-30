package com.baulsupp.okurl.services.mapbox

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class MapboxAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "api.mapbox.com", "Mapbox API", "mapbox",
      "https://www.mapbox.com/api-documentation/",
      "https://www.mapbox.com/studio/account/tokens/"
    )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url.newBuilder().addQueryParameter("access_token", token).build()

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

  override fun defaultCredentials(): Oauth2Token? = Oauth2Token(
    "pk.eyJ1IjoieXNjaGlta2UiLCJhIjoiY2tlb3E5MWIyMWp4eDJ2azdraWg5cHkxYyJ9.UHmWRzY_VE7gqIjCwIAmNA"
  )
}
