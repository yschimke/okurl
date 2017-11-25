package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.kotlin.okshell
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.twilio.TwilioAuthInterceptor
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TwilioTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()
  private val service = TwilioAuthInterceptor().serviceDefinition()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  @Throws(Throwable::class)
  fun completeEndpointShortCommand1() {
    credentialsStore.storeCredentials(BasicCredentials("ABC", "PW"), service)

    main.commandName = "okapi"
    main.arguments = newArrayList("commands/twilioapi", "/")
    main.urlComplete = true

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("/Calls.json"))
  }

  @Test
  @Throws(Throwable::class)
  fun completeEndpointWithReplacements() {
    credentialsStore.storeCredentials(BasicCredentials("ABC", "PW"), service)

    main.arguments = newArrayList("https://api.twilio.com/")
    main.urlComplete = true

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertTrue(output.stdout[0].contains("/Accounts/ABC/Calls.json"))
  }
}
