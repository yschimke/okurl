package com.baulsupp.okurl.credentials

interface CredentialsStore {
  suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: Token): T? {
    return tokenSet.name()?.let { get(serviceDefinition, it) }
  }

  suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T?

  suspend fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T)

  suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String)

  suspend fun <T : Any> findAllNamed(serviceDefinition: ServiceDefinition<T>): Map<String, T> {
    return names().keysToMapExceptNulls { get(serviceDefinition, it) }.toMap()
  }

  suspend fun names(): Set<String> {
    // TODO implement for other sources
    return setOf(DefaultToken.name)
  }

  companion object {
    val NONE: CredentialsStore = object : CredentialsStore {
      override suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? = null

      override suspend fun <T> set(serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T) {}

      override suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {}
    }

    private inline fun <K, V : Any> Iterable<K>.keysToMapExceptNulls(value: (K) -> V?): Map<K, V> {
      val map = LinkedHashMap<K, V>()
      for (k in this) {
        val v = value(k)
        if (v != null) {
          map[k] = v
        }
      }
      return map
    }
  }
}
