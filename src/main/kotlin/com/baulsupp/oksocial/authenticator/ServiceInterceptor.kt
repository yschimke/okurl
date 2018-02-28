package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.credentials.CredentialsStore
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.ServiceLoader

class ServiceInterceptor(private val authClient: OkHttpClient, private val credentialsStore: CredentialsStore) : Interceptor {
  private val services = ServiceLoader.load(AuthInterceptor::class.java, AuthInterceptor::class.java.classLoader).toList()

  override fun intercept(chain: Interceptor.Chain): Response {
    services
      .filter { it.supportsUrl(chain.request().url()) }
      .forEach { return runBlocking { intercept(it, chain) } }

    return chain.proceed(chain.request())
  }

  suspend fun <T> intercept(interceptor: AuthInterceptor<T>, chain: Interceptor.Chain): Response {
    val credentials = credentialsStore[interceptor.serviceDefinition()] ?: interceptor.defaultCredentials()

    if (credentials != null) {
      val result = interceptor.intercept(chain, credentials)

      if (result.code() in 400..499) {
        if (interceptor.canRenew(result) && interceptor.canRenew(credentials)) {
          val newCredentials = interceptor.renew(authClient, credentials)

          if (newCredentials != null) {
            credentialsStore[interceptor.serviceDefinition()] = newCredentials
          }
        }
      }

      // TODO retry request

      return result
    } else {
      return chain.proceed(chain.request())
    }
  }

  fun services(): List<AuthInterceptor<*>> = services

  fun getByName(authName: String): AuthInterceptor<*>? =
    services.firstOrNull { n -> n.name() == authName }

  fun getByUrl(url: String): AuthInterceptor<*>? {
    val httpUrl = HttpUrl.parse(url)

    return httpUrl?.let { services.firstOrNull { it.supportsUrl(httpUrl) } }
  }

  fun findAuthInterceptor(nameOrUrl: String): AuthInterceptor<*>? = getByName(nameOrUrl) ?: getByUrl(nameOrUrl)

  fun names(): List<String> = services.map { it.name() }
}
