package com.baulsupp.oksocial.services.httpbin

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * http://httpbin.org/
 */
class HttpBinAuthInterceptor : AuthInterceptor<BasicCredentials>() {

  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): BasicCredentials {
    val user = Secrets.prompt("User", "httpbin.user", "", false)
    val password = Secrets.prompt("Password", "httpbin.password", "", true)

    return BasicCredentials(user, password)
  }

  override fun serviceDefinition(): ServiceDefinition<BasicCredentials> =
    BasicAuthServiceDefinition("httpbin.org", "HTTP Bin", "httpbin",
      "https://httpbin.org/", null)

  override suspend fun validate(client: OkHttpClient,
                                credentials: BasicCredentials): ValidatedCredentials =
    ValidatedCredentials(credentials.user, null)

  override fun hosts(): Set<String> = setOf((
    "httpbin.org")
  )
}
