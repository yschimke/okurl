package com.baulsupp.oksocial.services.stackexchange

import com.baulsupp.oksocial.credentials.ServiceDefinition

class StackExchangeServiceDefinition : ServiceDefinition<StackExchangeToken> {
  override fun apiHost() = "api.stackexchange.com"

  override fun serviceName() = "StackExchange API"

  override fun shortName() = "stackexchange"

  override fun parseCredentialsString(s: String): StackExchangeToken {
    val parts = s.split(":".toRegex(), 2).toTypedArray()
    return StackExchangeToken(parts[0], parts[1])
  }

  override fun formatCredentialsString(credentials: StackExchangeToken) =
      credentials.accessToken + ":" + credentials.key

  override fun apiDocs() = "https://api.stackexchange.com/docs"

  override fun accountsLink() = "http://stackapps.com/apps/oauth"
}
