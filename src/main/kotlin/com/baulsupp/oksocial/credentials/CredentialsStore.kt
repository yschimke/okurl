package com.baulsupp.oksocial.credentials

interface CredentialsStore {

  fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T?

  fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>)

  companion object {
    val NONE: CredentialsStore = object : CredentialsStore {
      override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? = null

      override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {}
    }
  }
}
