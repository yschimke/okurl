package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpsCompletion {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  fun completePeopleEndpointSite() {
    main.arguments = mutableListOf("https://")
    main.urlComplete = true

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://people.googleapis.com/"))
    assertTrue(output.stdout[0].contains("https://graph.facebook.com/"))
  }
}
