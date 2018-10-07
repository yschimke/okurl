package com.baulsupp.okurl.services.symphony

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.i9n.TestCredentialsStore
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymphonyCompletionTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  fun completeFoundationDev() {
    main.arguments = mutableListOf("https://founda")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertEquals(listOf("https://foundation-dev.symphony.com", "https://foundation-dev.symphony.com/", "https://foundation-dev-api.symphony.com", "https://foundation-dev-api.symphony.com/").joinToString("\n"), output.stdout[0])
  }

  @Test
  fun completeFoundationDevSite() {
    main.arguments = mutableListOf("https://foundation-dev.symphony.com/agent")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://foundation-dev.symphony.com/agent/v1/signals/list"))
    assertTrue(output.stdout[0].contains("https://foundation-dev.symphony.com/agent/v2/HealthCheck"))
    assertTrue(output.stdout[0].contains("https://foundation-dev.symphony.com/agent/v5/firehose/create"))
  }

  @Test
  fun completeKnownSite() {
    main.arguments = mutableListOf("https://baulsupp.symp")
    main.urlComplete = true
    credentialsStore.tokens[Pair("symphony", "baulsupp")] = SymphonyCredentials("baulsupp", "a.p12", "a", null, null).format()

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertEquals("https://baulsupp.symphony.com\nhttps://baulsupp.symphony.com/", output.stdout[0])
  }

  @Test
  fun completeFakeSite() {
    main.arguments = mutableListOf("https://fake.symphony.com/agent/v1/signals/c")
    main.urlComplete = true
    main.debug

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertEquals("https://fake.symphony.com/agent/v1/signals/create", output.stdout[0])
  }
}
