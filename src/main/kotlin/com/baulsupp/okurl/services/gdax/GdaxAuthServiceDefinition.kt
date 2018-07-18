package com.baulsupp.okurl.services.gdax

import com.baulsupp.okurl.services.AbstractServiceDefinition

object GdaxAuthServiceDefinition : AbstractServiceDefinition<GdaxCredentials>("api.gdax.com", "GDAX API", "gdax",
  "https://docs.gdax.com/", "https://www.gdax.com/settings/api") {

  override fun parseCredentialsString(s: String): GdaxCredentials {
    val parts = s.split(":".toRegex(), 3)
    return GdaxCredentials(parts[0], parts[1], parts[2])
  }

  override fun formatCredentialsString(credentials: GdaxCredentials) =
    "${credentials.apiKey}:${credentials.apiSecret}:${credentials.passphrase}"
}
