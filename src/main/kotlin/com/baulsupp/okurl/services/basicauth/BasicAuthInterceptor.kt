package com.baulsupp.okurl.services.basicauth

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BasicAuthInterceptor : AuthInterceptor<FilteredBasicCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<FilteredBasicCredentials>("basic", "Basic Auth", "basic",
    "https://en.wikipedia.org/wiki/Basic_access_authentication") {

    override fun parseCredentialsString(s: String): FilteredBasicCredentials {
      val (user, password, hostPattern) = s.split(":".toRegex(), 3).toTypedArray()
      return FilteredBasicCredentials(BasicCredentials(user, password), hostPattern)
    }

    override fun formatCredentialsString(credentials: FilteredBasicCredentials) =
      "${credentials.basicCredentials.user}:${credentials.basicCredentials.password}:${credentials.hostPattern}"
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: FilteredBasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder().addHeader("Authorization", credentials.basicCredentials.header()).build()

    return chain.proceed(request)
  }

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean =
    FilteredBasicCredentials.matches(credentialsStore.findAllNamed(serviceDefinition).values, url)

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): FilteredBasicCredentials {
    val user = Secrets.prompt("Basic Auth User", "basic.user", "", false)
    val password = Secrets.prompt("Basic Auth Password", "basic.password", "", true)
    val hostPattern = Secrets.prompt("Matching Hosts *|*.site.com|app.site.com", "basic.hostpattern", "", false)

    return FilteredBasicCredentials(BasicCredentials(user, password), hostPattern)
  }

  override fun hosts(): Set<String> = setOf("basic")

  // lower priority than any site specific auth
  override val priority: Int
    get() = -100
}
