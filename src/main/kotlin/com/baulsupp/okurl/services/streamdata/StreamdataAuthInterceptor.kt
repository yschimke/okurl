package com.baulsupp.okurl.services.streamdata

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class StreamdataAuthInterceptor : AuthInterceptor<String>() {
  override val serviceDefinition = object : AbstractServiceDefinition<String>(
    "streamdata.motwin.net", "Streamdata", "streamdata",
    "https://streamdata.io/developers/docs/", "https://portal.streamdata.io/#/home"
  ) {
    override fun parseCredentialsString(s: String): String = s

    override fun formatCredentialsString(credentials: String): String = credentials
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: String): Response {
    var request = chain.request()

    val signedUrl = request.url.newBuilder().addQueryParameter("X-Sd-Token", credentials).build()

    request = request.newBuilder().url(signedUrl).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): String =
    Secrets.prompt("Streamdata App Token", "streamdata.appKey", "", false)

  override suspend fun validate(
    client: OkHttpClient,
    credentials: String
  ): ValidatedCredentials =
    ValidatedCredentials(credentials, null)

  override fun hosts(credentialsStore: CredentialsStore): Set<String> =
    setOf("streamdata.motwin.net", "stockmarket.streamdata.io", "streamdata.motwin.net")
}
