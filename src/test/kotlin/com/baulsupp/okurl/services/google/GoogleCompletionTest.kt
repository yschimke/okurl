package com.baulsupp.okurl.services.google

import com.baulsupp.oksocial.output.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.i9n.TestCredentialsStore
import com.baulsupp.okurl.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleCompletionTest {
  private val main = Main().apply {
    credentialsStore = TestCredentialsStore()
  }
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
  }

  @Test
  fun completePeopleEndpointSite() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://people.googleapis.com/")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://people.googleapis.com/"))
  }

  @Test
  fun completePeopleEndpointPath() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://people.googleapis.com/v1/people:batch")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://people.googleapis.com/v1/people:batchGet"))
  }

  @Test
  @Disabled
  fun completeGmailUserId() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://www.googleapis.com/gmail/v1/")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(
        output.stdout[0].contains("https://www.googleapis.com/gmail/v1/users/me/profile"))
  }

  // Nested example
  @Test
  @Disabled
  fun completeGmailMessages() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://www.googleapis.com/gmail/v1/")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(
        output.stdout[0].contains("https://www.googleapis.com/gmail/v1/users/me/messages"))
  }
}
