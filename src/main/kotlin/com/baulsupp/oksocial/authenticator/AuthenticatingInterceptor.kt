package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.kotlin.client
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.ServiceLoader

class AuthenticatingInterceptor(private val main: CommandLineClient, val services: List<AuthInterceptor<*>> = defaultServices()) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    services
      .filter { it.supportsUrl(chain.request().url()) }
      .forEach { return runBlocking { intercept(it, chain) } }

    return chain.proceed(chain.request())
  }

  suspend fun <T> intercept(interceptor: AuthInterceptor<T>, chain: Interceptor.Chain): Response {
    // TODO log bad tags?
    val tokenSet = chain.request().tag() as? Token ?: NoToken

    val credentials = when (tokenSet) {
      is TokenValue -> interceptor.serviceDefinition.castToken(tokenSet.token)
      is NoToken -> null
      else -> main.credentialsStore.get(interceptor.serviceDefinition, tokenSet) ?: interceptor.defaultCredentials()
    }

    if (credentials != null) {
      val result = interceptor.intercept(chain, credentials)

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

      // TODO retry request

      return result
    } else {
      return chain.proceed(chain.request())
    }
  }

  fun getByName(authName: String): AuthInterceptor<*>? =
    services.firstOrNull { n -> n.name() == authName }

  fun getByUrl(url: String): AuthInterceptor<*>? {
    val httpUrl = HttpUrl.parse(url)

    return httpUrl?.let { services.firstOrNull { it.supportsUrl(httpUrl) } }
  }

  fun findAuthInterceptor(nameOrUrl: String): AuthInterceptor<*>? = getByName(nameOrUrl) ?: getByUrl(nameOrUrl)

  fun names(): List<String> = services.map { it.name() }

  companion object {
    fun defaultServices() = ServiceLoader.load(AuthInterceptor::class.java, AuthInterceptor::class.java.classLoader).toList()
  }
}
