package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists.newArrayList
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

class ServiceApiDocPresenterTest {
  private val outputHandler = TestOutputHandler<Any>()
  private val client = OkHttpClient()
  private val credentialsStore = CredentialsStore.NONE
  private val presenter = ServiceApiDocPresenter(ServiceInterceptor(client, credentialsStore),
      client,
      credentialsStore)

  @Test
  @Throws(IOException::class)
  fun returnsAllUrls() {
    presenter.explainApi("https://api1.test.com/me", outputHandler, client)

    assertEquals(newArrayList("Test: https://api1.test.com/me"), outputHandler.stdout)
  }

  @Test
  @Throws(IOException::class)
  fun errorForUnknown() {
    presenter.explainApi("https://api1.blah.com/me", outputHandler, client)

    assertEquals(newArrayList("No documentation for: https://api1.blah.com/me"),
        outputHandler.stdout)
  }
}
