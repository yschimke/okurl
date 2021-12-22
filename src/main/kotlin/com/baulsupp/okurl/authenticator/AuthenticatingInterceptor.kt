package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.credentials.*
import com.baulsupp.okurl.services.ServiceLibrary
import com.baulsupp.okurl.services.ServiceList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.util.logging.Logger

// TODO log bad tags?
fun Interceptor.Chain.token() = request().tag(Token::class.java) ?: NoToken

suspend fun <T> credentials(
  tokenSet: Token,
  interceptor: AuthInterceptor<T>,
  credentialsStore: CredentialsStore
): T? {
  val serviceDefinition: ServiceDefinition<T> = interceptor.serviceDefinition
  return when (tokenSet) {
    is TokenValue -> serviceDefinition.castToken(tokenSet.token)
    is NoToken -> null
    else -> credentialsStore.get(serviceDefinition, tokenSet)
      ?: interceptor.defaultCredentials()
  }
}

class AuthenticatingInterceptor(
  private val credentialsStore: CredentialsStore,
  override val services: List<AuthInterceptor<*>> = ServiceList.defaultServices()
) : Interceptor, ServiceLibrary {
  override fun intercept(chain: Interceptor.Chain): Response {
    return runBlocking(Dispatchers.IO) {
      val firstInterceptor =
        services.find { it.supportsUrl(chain.request().url, credentialsStore) }

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
    val httpUrl = url.toHttpUrlOrNull()

    return httpUrl?.run {
      runBlocking {
        services.find {
          it.supportsUrl(httpUrl, credentialsStore)
        }
      }
    }
  }

  override fun findAuthInterceptor(name: String): AuthInterceptor<*>? = getByName(name)

  fun names(): List<String> = services.map { it.name() }

  companion object {
    val logger: Logger = Logger.getLogger(AuthenticatingInterceptor::class.java.name)
  }
}
