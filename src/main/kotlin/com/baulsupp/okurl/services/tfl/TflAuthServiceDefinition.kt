package com.baulsupp.okurl.services.tfl

import com.baulsupp.okurl.services.AbstractServiceDefinition

object TflAuthServiceDefinition : AbstractServiceDefinition<TflCredentials>("api.tfl.gov.uk", "TFL API", "tfl",
  "https://api-portal.tfl.gov.uk/docs", "https://api-portal.tfl.gov.uk/admin/applications/") {

  override fun parseCredentialsString(s: String): TflCredentials {
    val parts = s.split(":".toRegex(), 2)
    return TflCredentials(parts[0], parts[1])
  }

  override fun formatCredentialsString(credentials: TflCredentials) =
    "${credentials.appId}:${credentials.apiKey}"
}
