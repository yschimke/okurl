package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.commands.ToolSession
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.services.ServiceLibrary
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.ServiceLoader
import java.util.logging.Logger

class AuthenticatingInterceptor(
  private val main: ToolSession,
  override val services: List<AuthInterceptor<*>> = defaultServices()
) : Interceptor,
  ServiceLibrary {
  override fun intercept(chain: Interceptor.Chain): Response {
    return runBlocking {
      val filteredAuthenticators = services
        .filter { it.supportsUrl(chain.request().url(), main.credentialsStore) }

      logger.fine { "Matching interceptors: $filteredAuthenticators" }

      if (filteredAuthenticators.isNotEmpty()) {
        intercept(filteredAuthenticators.first(), chain)
      } else {
        chain.proceed(chain.request())
      }
    }
  }

  override fun knownServices(): Set<String> {
    return services.map { it.name() }.toSortedSet()
  }

  suspend fun <T> intercept(interceptor: AuthInterceptor<T>, chain: Interceptor.Chain): Response {
    // TODO log bad tags?
    val tokenSet = chain.request().tag(Token::class.java) ?: NoToken

    val credentials = when (tokenSet) {
      is TokenValue -> interceptor.serviceDefinition.castToken(tokenSet.token)
      is NoToken -> null
      else -> main.credentialsStore.get(interceptor.serviceDefinition, tokenSet) ?: interceptor.defaultCredentials()
    }

    val result = interceptor.intercept(chain, credentials, main.credentialsStore)

    // TODO move inside auth interceptor
    if (credentials != null) {
      val tokenSetName = tokenSet.name()
      if (tokenSetName != null) {
        if (result.code() in 400..499) {
          if (interceptor.canRenew(result) && interceptor.canRenew(credentials)) {
            val newCredentials = interceptor.renew(client, credentials)

            if (newCredentials != null) {
              main.credentialsStore.set(interceptor.serviceDefinition, tokenSetName, newCredentials)
            }
          }
        }
      }
    }

    return result
  }

  fun getByName(authName: String): AuthInterceptor<*>? =
    services.firstOrNull { n -> n.name() == authName }

  fun getByUrl(url: String): AuthInterceptor<*>? {
    val httpUrl = HttpUrl.parse(url)

    return httpUrl?.run { runBlocking { services.find { it.supportsUrl(httpUrl, main.credentialsStore) } } }
  }

  override fun findAuthInterceptor(name: String): AuthInterceptor<*>? = getByName(name) ?: getByUrl(name)

  fun names(): List<String> = services.map { it.name() }

  companion object {
    val logger: Logger = Logger.getLogger(AuthenticatingInterceptor::class.java.name)

    fun defaultServices() = ServiceLoader.load(AuthInterceptor::class.java, AuthInterceptor::class.java.classLoader)
      .sortedBy { -it.priority }
  }
}
