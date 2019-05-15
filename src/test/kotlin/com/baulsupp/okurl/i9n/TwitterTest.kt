package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.util.TestUtil.projectFile
import kotlinx.coroutines.runBlocking
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

    runBlocking {
      main.run()
    }

    if (output.failures.isNotEmpty()) {
      throw output.failures[0]
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens[Pair("twitter", DefaultToken.name)])
  }

  @Test
  fun importFromTwurl() {
    main.authorize = true
    main.arguments = mutableListOf("twitter", "--twurlrc",
        projectFile("src/test/resources/single_twurlrc.yaml").absolutePath)

    runBlocking {
      main.run()
    }

    if (output.failures.isNotEmpty()) {
      throw output.failures[0]
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens[Pair("twitter", DefaultToken.name)])
  }
}
