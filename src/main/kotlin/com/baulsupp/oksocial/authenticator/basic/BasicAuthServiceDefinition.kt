package com.baulsupp.oksocial.authenticator.basic

import com.baulsupp.oksocial.AbstractServiceDefinition
import com.baulsupp.oksocial.authenticator.BasicCredentials

class BasicAuthServiceDefinition(apiHost: String, serviceName: String, shortName: String,
                                 apiDocs: String, accountsLink: String?) : AbstractServiceDefinition<BasicCredentials>(apiHost, serviceName, shortName, apiDocs, accountsLink) {

  override fun parseCredentialsString(s: String): BasicCredentials {
    val parts = s.split(":".toRegex(), 2).toTypedArray()
    return BasicCredentials(parts[0], parts[1])
  }

  override fun formatCredentialsString(credentials: BasicCredentials) =
      credentials.user + ":" + credentials.password
}
