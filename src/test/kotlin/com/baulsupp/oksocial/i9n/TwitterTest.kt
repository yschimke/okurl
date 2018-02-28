package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TwitterTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
    main.resolve = listOf()
  }

  @Test
  fun setToken() {
    main.authorize = true
    main.token = "PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET"
    main.arguments = mutableListOf("twitter")

    main.run()

    if (!output.failures.isEmpty()) {
      throw output.failures[0]
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens["api.twitter.com"])
  }

  @Test
  fun importFromTwurl() {
    main.authorize = true
    main.arguments = mutableListOf("twitter", "--twurlrc",
        "src/test/resources/single_twurlrc.yaml")

    main.run()

    if (!output.failures.isEmpty()) {
      throw output.failures[0]
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens["api.twitter.com"])
  }
}
