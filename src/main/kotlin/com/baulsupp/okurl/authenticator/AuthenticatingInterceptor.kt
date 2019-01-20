package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.services.ServiceLibrary
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.ServiceLoader
import java.util.logging.Logger

// TODO log bad tags?
fun Interceptor.Chain.token() = request().tag(Token::class.java) ?: NoToken

suspend fun <T> credentials(
  tokenSet: Token,
  interceptor: AuthInterceptor<T>,
  credentialsStore: CredentialsStore
): T? {
  return when (tokenSet) {
    is TokenValue -> interceptor.serviceDefinition.castToken(tokenSet.token)
    is NoToken -> null
    else -> credentialsStore.get(interceptor.serviceDefinition, tokenSet) ?: interceptor.defaultCredentials()
  }
}

class AuthenticatingInterceptor(
  private val credentialsStore: CredentialsStore,
  override val services: List<AuthInterceptor<*>> = defaultServices()
) : Interceptor, ServiceLibrary {
  override fun intercept(chain: Interceptor.Chain): Response {
    return runBlocking {
      val firstInterceptor = services.find { it.supportsUrl(chain.request().url(), credentialsStore) }

      logger.fine { "Matching interceptor: $firstInterceptor" }

      if (firstInterceptor != null) {
        intercept(firstInterceptor, chain)
      } else {
        chain.proceed(chain.request())
      }
    }
  }

  override fun knownServices(): Set<String> {
    return services.map { it.name() }.toSortedSet()
  }

  suspend fun <T> intercept(interceptor: AuthInterceptor<T>, chain: Interceptor.Chain): Response {
    val tokenSet = chain.token()

    val credentials = credentials(tokenSet, interceptor, credentialsStore)

    return interceptor.intercept(chain, credentials, credentialsStore)
  }

  fun getByName(authName: String): AuthInterceptor<*>? =
    services.firstOrNull { n -> n.name() == authName }

  fun getByUrl(url: String): AuthInterceptor<*>? {
    val httpUrl = HttpUrl.parse(url)

    return httpUrl?.run { runBlocking { services.find { it.supportsUrl(httpUrl, credentialsStore) } } }
  }

  override fun findAuthInterceptor(name: String): AuthInterceptor<*>? = getByName(name) ?: getByUrl(name)

  fun names(): List<String> = services.map { it.name() }

  companion object {
    val logger: Logger = Logger.getLogger(AuthenticatingInterceptor::class.java.name)

    @Suppress("UNCHECKED_CAST")
    fun defaultServices(): List<AuthInterceptor<Any>> {
      val base = ServiceLoader.load(AuthInterceptor::class.java, AuthInterceptor::class.java.classLoader)
      return (base as ServiceLoader<AuthInterceptor<Any>>).sortedBy { -it.priority }
    }
  }
}
