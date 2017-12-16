package com.baulsupp.oksocial.credentials

class FixedTokenCredentialsStore(private val token: String) : CredentialsStore {

  override fun <T> get(serviceDefinition: ServiceDefinition<T>): T? {
    return serviceDefinition.parseCredentialsString(token)
  }

  override fun <T> set(
          serviceDefinition: ServiceDefinition<T>, credentials: T) {
    throw UnsupportedOperationException()
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>) {
  }
}
