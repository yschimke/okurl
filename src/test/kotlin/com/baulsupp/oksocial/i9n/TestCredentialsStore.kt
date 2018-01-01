package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition


class TestCredentialsStore : CredentialsStore {
  var tokens: MutableMap<String, String> = linkedMapOf()

  override fun <T> get(serviceDefinition: ServiceDefinition<T>): T? {
    return tokens[serviceDefinition.apiHost()]
        ?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override fun <T> set(
          serviceDefinition: ServiceDefinition<T>, credentials: T) {
    tokens.put(serviceDefinition.apiHost(), serviceDefinition.formatCredentialsString(credentials))
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>) {
    tokens.remove(serviceDefinition.apiHost())
  }
}
