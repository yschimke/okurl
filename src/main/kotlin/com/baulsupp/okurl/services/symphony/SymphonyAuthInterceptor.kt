package com.baulsupp.okurl.services.symphony

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SymphonyAuthInterceptor : AuthInterceptor<String>() {
  override val serviceDefinition = object : AbstractServiceDefinition<String>("foundation-dev.symphony.com", "Symphony", "symphony",
    "https://rest-api.symphony.com/") {
    override fun parseCredentialsString(s: String): String = s

    override fun formatCredentialsString(credentials: String): String = credentials
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: String): Response {
    var request = chain.request()

    // noop
    request = request.newBuilder().build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): String =
    TODO()

  override suspend fun validate(
    client: OkHttpClient,
    credentials: String
  ): ValidatedCredentials =
    ValidatedCredentials(credentials, null)

  override fun hosts(): Set<String> = setOf("foundation-dev.symphony.com", "foundation-dev-api.symphony.com")
}
