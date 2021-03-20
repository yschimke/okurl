package com.baulsupp.okurl.services.basicauth

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlCompleter
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BasicAuthInterceptor : AuthInterceptor<FilteredBasicCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<FilteredBasicCredentials>(
    "basic", "Basic Auth", "basic",
    "https://en.wikipedia.org/wiki/Basic_access_authentication"
  ) {

    override fun parseCredentialsString(s: String): FilteredBasicCredentials {
      val (user, password, hostPattern) = s.split(":".toRegex(), 3).toTypedArray()
      return FilteredBasicCredentials(BasicCredentials(user, password), hostPattern)
    }

    override fun formatCredentialsString(credentials: FilteredBasicCredentials) =
      "${credentials.basicCredentials.user}:${credentials.basicCredentials.password}:${credentials.hostPattern}"
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: FilteredBasicCredentials): Response {
    val request = chain.request().newBuilder().addHeader("Authorization", credentials.basicCredentials.header()).build()
    return chain.proceed(request)
  }

  override suspend fun intercept(
    chain: Interceptor.Chain,
    credentials: FilteredBasicCredentials?,
    credentialsStore: CredentialsStore
  ): Response {
    var request = chain.request()
    val url = chain.call().request().url

    val matchingCredentials: FilteredBasicCredentials?

    matchingCredentials = if (credentials?.matches(url) == true) {
      credentials
    } else {
      FilteredBasicCredentials.firstMatch(allStoredCredentials(credentialsStore), url)
    }

    if (matchingCredentials != null) {
      request = request.newBuilder().addHeader("Authorization", matchingCredentials.basicCredentials.header()).build()
    }

    return chain.proceed(request)
  }

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean =
    FilteredBasicCredentials.matches(allStoredCredentials(credentialsStore), url)

  suspend fun allStoredCredentials(credentialsStore: CredentialsStore) =
    credentialsStore.findAllNamed(serviceDefinition).values

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

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ) = UrlCompleter.NullCompleter

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("basic")

  // lower priority than any site specific auth
  override val priority: Int
    get() = -100
}
