package com.baulsupp.oksocial.credentials

class FixedTokenCredentialsStore(private val token: String) : CredentialsStore {

  override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
    return serviceDefinition.parseCredentialsString(token)
  }

  override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
    throw UnsupportedOperationException()
  }
}
