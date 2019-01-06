package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.edit
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.logging.Logger

object Retried

fun Request.isRetry() = tag(Retried::class.java) != null
fun Request.markAsRetry() = this.edit { tag(Retried::class.java, Retried) }

class RenewingInterceptor(
  private val credentialsStore: CredentialsStore,
  private val services: List<AuthInterceptor<*>> = AuthenticatingInterceptor.defaultServices()
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val response = chain.proceed(request)

    if (!request.isRetry() && response.code() in 400..499) {
      runBlocking {
        val firstInterceptor = services.find { it.supportsUrl(request.url(), credentialsStore) }

        if (firstInterceptor != null) {
          if (renew(firstInterceptor, chain.token(), response)) {
            logger.fine("Reattempting request")
            chain.proceed(request.markAsRetry())
          }
        }
      }
    }

    return response
  }

  private suspend fun <T> renew(
    interceptor: AuthInterceptor<T>,
    tokenSet: Token,
    response: Response
  ): Boolean {
    val credentials = credentials(tokenSet, interceptor, credentialsStore)

    if (credentials != null) {
      val tokenSetName = tokenSet.name()
      if (tokenSetName != null) {
        if (interceptor.canRenew(response) && interceptor.canRenew(credentials)) {
          val newCredentials = interceptor.renew(client, credentials)

          if (newCredentials != null) {
            logger.fine("Setting renewed credentials")
            credentialsStore.set(interceptor.serviceDefinition, tokenSetName, newCredentials)
            return true
          }
        }
      }
    }

    return false
  }

  companion object {
    val logger: Logger = Logger.getLogger(RenewingInterceptor::class.java.name)
  }
}
