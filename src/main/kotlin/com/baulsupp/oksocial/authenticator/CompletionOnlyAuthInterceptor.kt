package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CompletionOnlyAuthInterceptor : AuthInterceptor<Nothing> {
  override fun intercept(chain: Interceptor.Chain, credentials: Nothing): Response =
      chain.proceed(chain.request())

  override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>, authArguments: List<String>): Nothing =
      throw IOException("authorize not supported")

  override fun serviceDefinition(): ServiceDefinition<Nothing> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun validate(client: OkHttpClient, requestBuilder: Request.Builder, credentials: Nothing): Future<ValidatedCredentials> {
    CompletableFuture.completedFuture(ValidatedCredentials(null, null))
  }

  override fun hosts(): Collection<String> {
    return listOf("fivethirtyeight.datasettes.com", "parlgov.datasettes.com")
  }
}