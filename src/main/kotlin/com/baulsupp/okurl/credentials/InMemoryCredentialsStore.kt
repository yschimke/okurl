package com.baulsupp.okurl.credentials

import java.util.concurrent.ConcurrentHashMap

// TODO use token set
class InMemoryCredentialsStore(private val credentialsMap: MutableMap<String, String> = ConcurrentHashMap()) : CredentialsStore {
  override suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? {
    return credentialsMap[serviceDefinition.shortName()]?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override suspend fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T) {
    credentialsMap[serviceDefinition.shortName()] = serviceDefinition.formatCredentialsString(credentials)
  }

  override suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {
    credentialsMap.remove(serviceDefinition.shortName())
  }

  operator fun set(key: String, value: String) {
    credentialsMap[key] = value
  }
}
