package com.baulsupp.okurl.authenticator.basic

import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.services.AbstractServiceDefinition

class BasicAuthServiceDefinition(
  apiHost: String,
  serviceName: String,
  shortName: String,
  apiDocs: String? = null,
  accountsLink: String? = null
) : AbstractServiceDefinition<BasicCredentials>(apiHost, serviceName, shortName, apiDocs, accountsLink) {

  override fun parseCredentialsString(s: String): BasicCredentials {
    val (user, password) = s.split(":".toRegex(), 2).toTypedArray()
    return BasicCredentials(user, password)
  }

  override fun formatCredentialsString(credentials: BasicCredentials) =
    "${credentials.user}:${credentials.password}"
}
