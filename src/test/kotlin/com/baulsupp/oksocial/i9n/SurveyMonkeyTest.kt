package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.DefaultToken
import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.surveymonkey.SurveyMonkeyAuthInterceptor
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SurveyMonkeyTest {

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
    main.arguments = mutableListOf("https://api.surveymonkey.net/")
    main.urlComplete = true
    completionCache["surveymonkey", "survey"] = listOf("AA", "BB")
    credentialsStore.set(SurveyMonkeyAuthInterceptor().serviceDefinition(), DefaultToken.name, Oauth2Token(""))

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("/v3/surveys/AA/details"))
    assertTrue(output.stdout[0].contains("/v3/surveys/BB/details"))
  }
}
