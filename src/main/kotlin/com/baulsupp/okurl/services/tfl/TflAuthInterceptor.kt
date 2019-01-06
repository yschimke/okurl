package com.baulsupp.okurl.services.tfl

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TflAuthInterceptor : AuthInterceptor<TflCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<TflCredentials>(
    "api.tfl.gov.uk", "TFL API", "tfl",
    "https://api-portal.tfl.gov.uk/docs", "https://api-portal.tfl.gov.uk/admin/applications/"
  ) {

    override fun parseCredentialsString(s: String): TflCredentials {
      val parts = s.split(":".toRegex(), 2)
      return TflCredentials(parts[0], parts[1])
    }

    override fun formatCredentialsString(credentials: TflCredentials) =
      "${credentials.appId}:${credentials.apiKey}"
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: TflCredentials): Response {
    var request = chain.request()

    val signedUrl = request.url().newBuilder().addQueryParameter("app_id", credentials.appId)
      .addQueryParameter("app_key", credentials.apiKey).build()

    request = request.newBuilder().url(signedUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): TflCredentials {
    val apiKey = Secrets.prompt("TFL APP Id", "tfl.appId", "", false)
    val apiSecret = Secrets.prompt("TFL API Key", "tfl.apiKey", "", true)

    return TflCredentials(apiKey, apiSecret)
  }
}
