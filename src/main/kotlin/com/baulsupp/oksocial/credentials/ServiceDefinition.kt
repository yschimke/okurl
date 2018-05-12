package com.baulsupp.oksocial.credentials

interface ServiceDefinition<T> {
  fun apiHost(): String

  fun serviceName(): String

  fun parseCredentialsString(s: String): T

  fun formatCredentialsString(credentials: T): String

  fun shortName(): String

  fun apiDocs(): String? = null

  fun accountsLink(): String? = null

  fun castToken(token: Any): T {
    return token as T
  }
}
