package com.baulsupp.okurl.services.soundcloud

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SoundcloudAuthInterceptor : AuthInterceptor<SoundcloudCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<SoundcloudCredentials>(
    "api.soundcloud.com", "Soundcloud API", "soundcloud",
    "https://developers.soundcloud.com/docs/api/guide", "https://soundcloud.com/you/apps"
  ) {

    override fun parseCredentialsString(s: String): SoundcloudCredentials {
      val parts = s.split(":".toRegex(), 2)
      return SoundcloudCredentials(parts[0], parts[1].ifEmpty { null })
    }

    override fun formatCredentialsString(credentials: SoundcloudCredentials) =
      "${credentials.clientId}:${credentials.token.orEmpty()}"
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    return setOf("api.soundcloud.com", "api-v2.soundcloud.com", "api-mobile.soundcloud.com")
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: SoundcloudCredentials): Response {
    var request = chain.request()

    val signedUrl = request.url.newBuilder()
      .addQueryParameter("client_id", credentials.clientId)
      .apply {
        if (credentials.token != null) {
          addQueryParameter("oauth_token", credentials.token)
        }
      }
      .build()

    request = request.newBuilder()
      .url(signedUrl)
      .header("Referer", "https://soundcloud.com/stream")
      .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:94.0) Gecko/20100101 Firefox/94.0")
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): SoundcloudCredentials {
    val apiKey = Secrets.prompt("Soundcloud ClientId", "soundcloud.clientId", "", false)
    val apiSecret = Secrets.prompt("Soundcloud Token", "soundcloud.token", "", true)

    return SoundcloudCredentials(apiKey, apiSecret.ifEmpty { null })
  }
}
