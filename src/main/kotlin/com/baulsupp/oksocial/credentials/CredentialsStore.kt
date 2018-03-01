package com.baulsupp.oksocial.credentials

import org.jetbrains.kotlin.utils.keysToMapExceptNulls

interface CredentialsStore {

  fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String?): T?

  fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String?, credentials: T)

  fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String?)

  suspend fun <T : Any> findAllNamed(serviceDefinition: ServiceDefinition<T>): Map<String?, T> {
    val allNames = listOf(null) + names()

    return allNames.keysToMapExceptNulls { get(serviceDefinition, it) }
  }

  suspend fun names(): Set<String> {
    return setOf()
  }

  companion object {
    val NONE: CredentialsStore = object : CredentialsStore {
      override fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String?): T? = null

      override fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String?, credentials: T) {}

      override fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String?) {}
    }
  }
}
