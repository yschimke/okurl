package com.baulsupp.okurl.services.twitter.twurlrc

import com.baulsupp.okurl.services.twitter.TwitterCredentials
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

class TwurlCredentialsStore(val file: File) {

  fun readTwurlRc(): TwurlRc? = try {
    if (file.isFile) {
      val objectMapper = ObjectMapper(YAMLFactory())
      objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

      objectMapper.readValue(file, TwurlRc::class.java)
    } else {
      null
    }
  } catch (e: Exception) {
    throw RuntimeException(e)
  }

  fun readCredentials(): TwitterCredentials? = readTwurlRc()?.let {
    val username = it.defaultProfile()[0]
    val consumerKey = it.defaultProfile()[1]

    it.readCredentials(username, consumerKey)
  }

  companion object {
    var TWURL_STORE = TwurlCredentialsStore(File(System.getProperty("user.home"), ".twurlrc"))
  }
}
