package com.baulsupp.okurl.authenticator.oauth2

import com.baulsupp.okurl.services.AbstractServiceDefinition

class Oauth2ServiceDefinition(
  apiHost: String,
  serviceName: String,
  shortName: String,
  apiDocs: String? = null,
  accountsLink: String? = null
) : AbstractServiceDefinition<Oauth2Token>(apiHost, serviceName, shortName, apiDocs, accountsLink) {

  override fun parseCredentialsString(s: String): Oauth2Token {
    val parts = s.split(":")

    return if (parts.size < 4) {
      Oauth2Token(parts[0])
    } else {
      Oauth2Token(parts[0], parts[1], parts[2], parts[3])
    }
  }

  override fun formatCredentialsString(credentials: Oauth2Token): String {
    return if (credentials.refreshToken != null &&
      credentials.clientId != null &&
      credentials.clientSecret != null
    ) {
      "${credentials.accessToken}:${credentials.refreshToken}:${credentials.clientId}:${credentials.clientSecret}"
    } else {
      credentials.accessToken
    }
  }
}
