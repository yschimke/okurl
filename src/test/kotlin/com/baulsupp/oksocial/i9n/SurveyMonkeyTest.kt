package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.surveymonkey.SurveyMonkeyAuthInterceptor
import com.baulsupp.oksocial.services.surveymonkey.SurveyMonkeyToken
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Test
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
  @Throws(Throwable::class)
  fun completeEndpointWithReplacements() {
    main.arguments = newArrayList("https://api.surveymonkey.net/")
    main.urlComplete = true
    completionCache.store("surveymonkey", "surveys", Lists.newArrayList("AA", "BB"))
    credentialsStore.storeCredentials(SurveyMonkeyToken("", ""),
        SurveyMonkeyAuthInterceptor().serviceDefinition())

    main.run()

    assertEquals(mutableListOf(), output.failures)
    assertEquals(1, output.stdout.size)
    assertTrue(output.stdout[0].contains("/v3/surveys/AA/details"))
    assertTrue(output.stdout[0].contains("/v3/surveys/BB/details"))
  }
}
