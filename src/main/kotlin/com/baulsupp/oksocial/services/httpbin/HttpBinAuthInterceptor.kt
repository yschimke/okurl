package com.baulsupp.oksocial.services.httpbin

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import io.github.vjames19.futures.jdk8.ImmediateFuture
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

/**
 * http://httpbin.org/
 */
class HttpBinAuthInterceptor : AuthInterceptor<BasicCredentials> {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
        .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
        .build()

    return chain.proceed(request)
  }

  @Throws(IOException::class)
  override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): BasicCredentials {
    val user = Secrets.prompt("User", "httpbin.user", "", false)
    val password = Secrets.prompt("Password", "httpbin.password", "", true)

    return BasicCredentials(user, password)
  }

  override fun serviceDefinition(): ServiceDefinition<BasicCredentials> {
    return BasicAuthServiceDefinition("httpbin.org", "HTTP Bin", "httpbin",
        "https://httpbin.org/", null)
  }

  @Throws(IOException::class)
  override fun validate(client: OkHttpClient,
                        requestBuilder: Request.Builder, credentials: BasicCredentials): Future<ValidatedCredentials> {
    return ImmediateFuture { ValidatedCredentials(credentials.user, null) }
  }

  override fun hosts(): Collection<String> {
    return setOf((
        "httpbin.org")
    )
  }
}
