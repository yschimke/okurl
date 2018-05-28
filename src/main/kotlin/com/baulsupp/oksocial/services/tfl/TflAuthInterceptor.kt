package com.baulsupp.oksocial.services.tfl

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TflAuthInterceptor : AuthInterceptor<TflCredentials>() {
  override val serviceDefinition = TflAuthServiceDefinition

  override fun intercept(chain: Interceptor.Chain, credentials: TflCredentials): Response {
    var request = chain.request()

    val signedUrl = request.url().newBuilder().addQueryParameter("app_id", credentials.appId).addQueryParameter("app_key", credentials.apiKey).build()

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

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TflCredentials
  ): ValidatedCredentials = ValidatedCredentials()

  override fun canRenew(credentials: TflCredentials): Boolean = false

  override fun hosts(): Set<String> = setOf("api.tfl.gov.uk")
}
