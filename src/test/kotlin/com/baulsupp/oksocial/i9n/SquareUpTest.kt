package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.credentials.DefaultToken
import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.squareup.SquareUpAuthInterceptor
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Response
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
  fun completeEndpointWithReplacements() {
    main.arguments = mutableListOf("https://connect.squareup.com/")
    main.urlComplete = true
    completionCache["squareup", "location"] = listOf("AA", "bb")
    credentialsStore.set(SquareUpAuthInterceptor().serviceDefinition(), DefaultToken.name, Oauth2Token("test"))

    runBlocking {
      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("/v2/locations/AA/transactions"))
  }
}
