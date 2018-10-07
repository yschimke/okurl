package com.baulsupp.okurl.i9n

import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.ServiceDefinition

class TestCredentialsStore : CredentialsStore {
  var tokens: MutableMap<Pair<String, String?>, String> = linkedMapOf()

  override suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? {
    return tokens[Pair(serviceDefinition.shortName(), tokenSet)]
        ?.let { serviceDefinition.parseCredentialsString(it) }
  }

  override suspend fun names(): Set<String> {
    return setOf(DefaultToken.name) + tokens.mapNotNull { it.key.second }
  }

  override suspend fun <T> set(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String,
    credentials: T
  ) {
    tokens[Pair(serviceDefinition.shortName(), tokenSet)] = serviceDefinition.formatCredentialsString(credentials)
  }

  override suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {
    tokens.remove(Pair(serviceDefinition.shortName(), tokenSet))
  }
}
