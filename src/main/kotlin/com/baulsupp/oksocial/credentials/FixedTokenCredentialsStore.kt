package com.baulsupp.oksocial.credentials

class FixedTokenCredentialsStore(private val token: String) : CredentialsStore {

  override fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String?): T? {
    return serviceDefinition.parseCredentialsString(token)
  }

  override fun <T> set(
    serviceDefinition: ServiceDefinition<T>, tokenSet: String?, credentials: T) {
    throw UnsupportedOperationException()
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String?) {
  }
}
