package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.credentials.ServiceDefinition

class TwitterServiceDefinition : ServiceDefinition<TwitterCredentials> {
  override fun apiHost() = "api.twitter.com"

  override fun serviceName() = "Twitter API"

  override fun shortName() = "twitter"

  override fun apiDocs() = "https://developer.twitter.com/en/docs/api-reference-index"

  override fun accountsLink() = "https://developer.twitter.com/en/apps"

  override fun parseCredentialsString(s: String): TwitterCredentials {
    val list = s.split(',')

    if (list.size != 5) {
      throw IllegalStateException("can't split '$s'")
    }

    return TwitterCredentials(list[0], list[1], list[2], list[3], list[4])
  }

  override fun formatCredentialsString(credentials: TwitterCredentials) =
    "${credentials.username},${credentials.consumerKey},${credentials.consumerSecret},${credentials.token},${credentials.secret}"
}
