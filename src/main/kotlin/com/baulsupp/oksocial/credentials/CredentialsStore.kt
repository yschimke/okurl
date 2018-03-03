package com.baulsupp.oksocial.credentials

import com.baulsupp.oksocial.Token
import org.jetbrains.kotlin.utils.keysToMapExceptNulls

interface CredentialsStore {
  fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: Token): T? {
    TODO()
  }

  fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T?

  fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T)

  fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String)

  suspend fun <T : Any> findAllNamed(serviceDefinition: ServiceDefinition<T>): Map<String, T> {
    val allNames = names()

    return allNames.keysToMapExceptNulls { get(serviceDefinition, it) }
  }

  suspend fun names(): Set<String> {
    // TODO implement for other sources
    return setOf("default")
  }

  companion object {
    val NONE: CredentialsStore = object : CredentialsStore {
      override fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? = null

      override fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T) {}

      override fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {}
    }
  }
}
