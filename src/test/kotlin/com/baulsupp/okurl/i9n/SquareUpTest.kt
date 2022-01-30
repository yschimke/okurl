package com.baulsupp.okurl.i9n

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.services.squareup.SquareUpAuthInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SquareUpTest {

  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val completionCache = TestCompletionVariableCache()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.completionVariableCache = completionCache
    main.credentialsStore = credentialsStore
  }

  @Test
  @Disabled
  fun completeEndpointWithReplacements() {
    main.arguments = mutableListOf("https://connect.squareup.com/")
    main.urlComplete = true
    completionCache["squareup", "location"] = listOf("AA", "bb")

    runBlocking {
      credentialsStore.set(SquareUpAuthInterceptor().serviceDefinition, DefaultToken.name, Oauth2Token("test"))
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("/v2/locations/AA/transactions"))
  }
}
