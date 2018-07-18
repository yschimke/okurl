package com.baulsupp.okurl.authenticator.basic

import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.services.AbstractServiceDefinition

class BasicAuthServiceDefinition(
  apiHost: String,
  serviceName: String,
  shortName: String,
  apiDocs: String,
  accountsLink: String?
) : AbstractServiceDefinition<BasicCredentials>(apiHost, serviceName, shortName, apiDocs, accountsLink) {

  override fun parseCredentialsString(s: String): BasicCredentials {
    val parts = s.split(":".toRegex(), 2).toTypedArray()
    return BasicCredentials(parts[0], parts[1])
  }

  override fun formatCredentialsString(credentials: BasicCredentials) =
    credentials.user + ":" + credentials.password
}
