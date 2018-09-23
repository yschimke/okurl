package com.baulsupp.okurl.credentials

interface ServiceDefinition<T> {
  fun apiHost(): String

  fun serviceName(): String

  fun parseCredentialsString(s: String): T

  fun formatCredentialsString(credentials: T): String

  fun shortName(): String

  fun apiDocs(): String? = null

  fun accountsLink(): String? = null

  @Suppress("UNCHECKED_CAST")
  fun castToken(token: Any): T {
    return token as T
  }
}
