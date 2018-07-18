package com.baulsupp.okurl.services.twitter.twurlrc

import com.baulsupp.okurl.services.twitter.TwitterCredentials

class TwurlRc {
  var profiles: Map<String, Map<String, TwitterCredentials>>? = null
  var configuration: Map<String, List<String>>? = null

  fun defaultProfile(): List<String> {
    return configuration!!["default_profile"]!!
  }

  fun readCredentials(username: String, consumerKey: String): TwitterCredentials {
    return profiles!![username]!![consumerKey]!!
  }
}
