package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.AbstractServiceDefinition
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

abstract class CompletionOnlyAuthInterceptor(private val apiHost: String, private val serviceName: String, private val shortName: String, private val apiDocs: String) : AuthInterceptor<Nothing> {
  override fun intercept(chain: Interceptor.Chain, credentials: Nothing): Response =
      chain.proceed(chain.request())

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>, authArguments: List<String>): Nothing =
      throw IOException("authorize not supported")

  override suspend fun validate(client: OkHttpClient, requestBuilder: Request.Builder, credentials: Nothing): Future<ValidatedCredentials> =
      CompletableFuture.completedFuture(ValidatedCredentials(null, null))

  override fun serviceDefinition(): ServiceDefinition<Nothing> {
    return object : AbstractServiceDefinition<Nothing>(apiHost, serviceName, shortName,
        apiDocs, null) {
      override fun parseCredentialsString(s: String): Nothing {
        throw NotImplementedError()
      }

      override fun formatCredentialsString(credentials: Nothing): String {
        throw NotImplementedError()
      }
    }
  }
}