package com.baulsupp.okurl.services.deezer

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.soundcloud.SoundcloudCredentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class DeezerAuthInterceptor : Oauth2AuthInterceptor() {

  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.deezer.com", "Deezer API", "deezer",
    "https://developers.deezer.com/api",
    "https://developers.deezer.com/myapps"
  )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    return setOf("api.deezer.com")
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val signedUrl = request.url.newBuilder()
      .addQueryParameter("access_token", credentials.accessToken)
      .build()

    request = request.newBuilder()
      .url(signedUrl)
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val appId = Secrets.prompt("Deezer Application ID", "deezer.appId", "", false)
    val secretKey = Secrets.prompt("Deezer Secret Key", "deezer.secretKey", "", true)

    return DeezerAuthFlow.login(client, outputHandler, appId, secretKey)
  }
}
