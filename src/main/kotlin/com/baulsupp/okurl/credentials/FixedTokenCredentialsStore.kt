package com.baulsupp.okurl.credentials

import java.util.logging.Logger

class FixedTokenCredentialsStore(private val token: String) : CredentialsStore {
  private val logger = Logger.getLogger(FixedTokenCredentialsStore::class.java.name)

  override suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? {
    return try {
      serviceDefinition.parseCredentialsString(token)
    } catch (e: RuntimeException) {
      // assume not for this one
      null
    }
  }

  override suspend fun <T> set(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String,
    credentials: T
  ) {
    throw UnsupportedOperationException()
  }

  override suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {
  }
}
