package com.baulsupp.okurl.i9n

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.services.github.GithubAuthInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GithubTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()
  private val service = GithubAuthInterceptor().serviceDefinition

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  @Disabled("requires auth now")
  fun completeEndpoint() {
    runBlocking {
      credentialsStore.set(service, DefaultToken.name, Oauth2Token("ABC"))

      main.arguments = mutableListOf("https://api.github.com/")
      main.urlComplete = true

      main.run()
    }

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://api.github.com/user"))
  }
}
