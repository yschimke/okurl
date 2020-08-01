package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.services.test.TestAuthInterceptor
import com.baulsupp.okurl.services.twitter.TwitterAuthInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompletionTest {
  private val output = TestOutputHandler<Response>()
  private val store = TestCredentialsStore()
  private val cache = TestCompletionVariableCache()
  private val main = Main().apply {
    debug = true
    outputHandler = output
    credentialsStore = store
    completionVariableCache = cache
    authenticatingInterceptor = AuthenticatingInterceptor(store, listOf(
      TestAuthInterceptor(),
      TwitterAuthInterceptor()
    ))
    urlComplete = true
  }

  @Test
  fun completeEmpty() {
    main.arguments = mutableListOf("")

    runBlocking {
      main.run()
    }

    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("https://api1.test.com/"))
  }

  @Test
  fun completeSingleEndpoint() {
    main.arguments = mutableListOf("https://api1.test.co")

    runBlocking {
      main.run()
    }

    assertEquals(listOf(
      "https://api1.test.com\nhttps://api1.test.com/"),
      output.stdout)
  }

  @Test
  fun completeEndpointsForTwitterApi() {
    main.arguments = mutableListOf("https://api.twitter.com/")

    runBlocking {
      main.run()
    }

    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("\nhttps://api.twitter.com/1.1/geo/places.json\n"))
  }
}
