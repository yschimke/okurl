package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition

// TODO use token set
class TestCredentialsStore : CredentialsStore {
  var tokens: MutableMap<String, String> = linkedMapOf()

  override fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String?): T? {
    return tokens[serviceDefinition.apiHost()]
        ?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override fun <T> set(
          serviceDefinition: ServiceDefinition<T>, tokenSet: String?, credentials: T) {
    tokens.put(serviceDefinition.apiHost(), serviceDefinition.formatCredentialsString(credentials))
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String?) {
    tokens.remove(serviceDefinition.apiHost())
  }
}
