package com.baulsupp.oksocial.credentials

import java.util.concurrent.ConcurrentHashMap

class InMemoryCredentialsStore(private val credentialsMap: MutableMap<String, String> = ConcurrentHashMap()) : CredentialsStore {
  override fun <T> get(serviceDefinition: ServiceDefinition<T>): T? {
    return credentialsMap.get(serviceDefinition.shortName())?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override fun <T> set(serviceDefinition: ServiceDefinition<T>, credentials: T) {
    credentialsMap[serviceDefinition.shortName()] = serviceDefinition.formatCredentialsString(credentials)
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>) {
    credentialsMap.remove(serviceDefinition.shortName())
  }

  operator fun set(key: String, value: String) {
    credentialsMap[key] = value
  }
}