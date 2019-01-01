package com.baulsupp.okurl.services.httpbin

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://httpbin.org/
 */
class HttpBinAuthInterceptor : AuthInterceptor<BasicCredentials>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): BasicCredentials {
    val user = Secrets.prompt("User", "httpbin.user", "", false)
    val password = Secrets.prompt("Password", "httpbin.password", "", true)

    return BasicCredentials(user, password)
  }

  override val serviceDefinition =
    BasicAuthServiceDefinition(
      "httpbin.org", "HTTP Bin", "httpbin",
      "https://httpbin.org/", null
    )

  override suspend fun validate(
    client: OkHttpClient,
    credentials: BasicCredentials
  ): ValidatedCredentials =
    ValidatedCredentials(credentials.user, null)

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf(
    (
      "httpbin.org")
  )

  // lower priority than basic auth
  override val priority: Int
    get() = -200
}
