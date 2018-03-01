package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.github.GithubAuthInterceptor
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GithubTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()
  private val service = GithubAuthInterceptor().serviceDefinition()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  fun completeEndpointShortCommand1() {
    credentialsStore.set(service, null, Oauth2Token("ABC"))

    main.commandName = "okapi"
    main.arguments = mutableListOf("commands/githubapi", "/")
    main.urlComplete = true

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("/user"))
  }

  @Test
  fun completeEndpoint() {
    credentialsStore.set(service, null, Oauth2Token("ABC"))

    main.arguments = mutableListOf("https://api.github.com/")
    main.urlComplete = true

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("https://api.github.com/user"))
  }
}
