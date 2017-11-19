package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.google.common.collect.Maps

class TestCredentialsStore : CredentialsStore {
  var tokens: MutableMap<String, String> = Maps.newHashMap()

  override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
    return tokens[serviceDefinition.apiHost()]
        ?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
    tokens.put(serviceDefinition.apiHost(), serviceDefinition.formatCredentialsString(credentials))
  }
}
