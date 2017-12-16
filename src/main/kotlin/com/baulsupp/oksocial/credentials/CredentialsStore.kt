package com.baulsupp.oksocial.credentials

interface CredentialsStore {

  operator fun <T> get(serviceDefinition: ServiceDefinition<T>): T?

  operator fun <T> set(serviceDefinition: ServiceDefinition<T>, credentials: T)

  fun <T> remove(serviceDefinition: ServiceDefinition<T>)

  companion object {
    val NONE: CredentialsStore = object : CredentialsStore {
      override fun <T> get(serviceDefinition: ServiceDefinition<T>): T? = null

      override fun <T> set(serviceDefinition: ServiceDefinition<T>, credentials: T) {}

      override fun <T> remove(serviceDefinition: ServiceDefinition<T>) {}
    }
  }
}
