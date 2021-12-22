package com.baulsupp.okurl.services.soundcloud

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SoundcloudAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.soundcloud.com", "Soundcloud API", "soundcloud",
    "https://developers.soundcloud.com/docs/api/guide", "https://soundcloud.com/you/apps"
  )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    return setOf("api.soundcloud.com", "api-v2.soundcloud.com", "api-mobile.soundcloud.com")
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder()
      .header("Authorization", "OAuth ${credentials.accessToken}")
      .header("Referer", "https://soundcloud.com/stream")
      .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:94.0) Gecko/20100101 Firefox/94.0")
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val clientId = Secrets.prompt("Soundcloud ClientId", "soundcloud.clientId", "", false)
    val clientSecret = Secrets.prompt("Soundcloud ClientSecret", "soundcloud.clientSecret", "", true)

    return SoundcloudAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }
}
