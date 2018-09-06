package com.baulsupp.okurl.services.basicauth

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BasicAuthInterceptor : AuthInterceptor<BasicCredentials>() {
  override val serviceDefinition = BasicAuthServiceDefinition("basic", "Basic Auth", "basic",
    "https://en.wikipedia.org/wiki/Basic_access_authentication")

  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder().addHeader("Authorization", credentials.header()).build()

    return chain.proceed(request)
  }

  // temporarily working with https://httpbin.org/hidden-basic-auth/a/b
  override fun supportsUrl(url: HttpUrl): Boolean {
    return url.encodedPath().startsWith("/hidden-basic-auth/") || url.encodedPath().startsWith("/basic-auth/")
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): BasicCredentials {
    val user = Secrets.prompt("Basic Auth User", "basic.user", "", false)
    val password = Secrets.prompt("Basic Auth Password", "basic.password", "", false)

    return BasicCredentials(user, password)
  }

  override fun hosts(): Set<String> = setOf("basic")
}
