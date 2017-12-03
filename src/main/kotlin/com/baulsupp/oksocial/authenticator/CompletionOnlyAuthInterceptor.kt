package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

abstract class CompletionOnlyAuthInterceptor(private val apiHost: String, private val serviceName: String, private val shortName: String, private val apiDocs: String): AuthInterceptor<Nothing>() {
  override fun intercept(chain: Interceptor.Chain, credentials: Nothing): Response =
          chain.proceed(chain.request())

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): Nothing =
          throw IOException("authorize not supported")

  override suspend fun validate(client: OkHttpClient, credentials: Nothing): ValidatedCredentials =
          ValidatedCredentials(null, null)

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