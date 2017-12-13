package com.baulsupp.oksocial.credentials

import java.util.concurrent.ConcurrentHashMap

class InMemoryCredentialsStore(private val credentialsMap: MutableMap<String, String> = ConcurrentHashMap()): CredentialsStore {
  override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
    return credentialsMap.get(serviceDefinition.shortName())?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
    credentialsMap[serviceDefinition.shortName()] = serviceDefinition.formatCredentialsString(credentials)
  }

  operator fun set(key: String, value: String) {
    credentialsMap[key] = value
  }
}