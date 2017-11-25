package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists.newArrayList
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.assertEquals

class ServiceApiDocPresenterTest {
  private val outputHandler = TestOutputHandler<Any>()
  private val client = OkHttpClient()
  private val credentialsStore = CredentialsStore.NONE
  private val presenter = ServiceApiDocPresenter(ServiceInterceptor(client, credentialsStore))

  @Test
  fun returnsAllUrls() {
    runBlocking { presenter.explainApi("https://api1.test.com/me", outputHandler, client) }

    assertEquals(newArrayList("Test: https://api1.test.com/me"), outputHandler.stdout)
  }

  @Test
  fun errorForUnknown() {
    runBlocking { presenter.explainApi("https://api1.blah.com/me", outputHandler, client) }

    assertEquals(newArrayList("No documentation for: https://api1.blah.com/me"),
            outputHandler.stdout)
  }
}
