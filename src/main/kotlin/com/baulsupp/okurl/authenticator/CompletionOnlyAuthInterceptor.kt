package com.baulsupp.okurl.authenticator

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

abstract class CompletionOnlyAuthInterceptor(
  private val apiHost: String,
  private val serviceName: String,
  private val shortName: String,
  private val apiDocs: String
) : AuthInterceptor<Nothing>() {
  override suspend fun intercept(chain: Interceptor.Chain, credentials: Nothing): Response =
    chain.proceed(chain.request())

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Nothing =
    throw IOException("authorize not supported")

  override suspend fun validate(client: OkHttpClient, credentials: Nothing): ValidatedCredentials =
    ValidatedCredentials(null, null)

  override val serviceDefinition = object : AbstractServiceDefinition<Nothing>(
    apiHost, serviceName, shortName,
    apiDocs, null
  ) {
    override fun parseCredentialsString(s: String): Nothing {
      throw NotImplementedError()
    }

    override fun formatCredentialsString(credentials: Nothing): String {
      throw NotImplementedError()
    }
  }
}
