package com.baulsupp.okurl.apidocs

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.credentials.NoToken
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ServiceApiDocPresenterTest {
  private val main = Main()

  private val testOutputHandler = TestOutputHandler<Any>()

  init {
    main.outputHandler = testOutputHandler
    main.initialise()
  }

  val presenter =
    ServiceApiDocPresenter(AuthenticatingInterceptor(main.credentialsStore, main.authenticatingInterceptor.services))

  @Test
  fun returnsAllUrls() {
    runBlocking {
      presenter.explainApi(
        "https://api1.test.com/me", main.outputHandler, main.client,
        NoToken
      )
    }

    assertEquals(mutableListOf("Test: https://api1.test.com/me"), testOutputHandler.stdout)
  }

  @Test
  fun errorForUnknown() {
    runBlocking {
      presenter.explainApi(
        "https://api1.blah.com/me", main.outputHandler, main.client,
        NoToken
      )
    }

    assertEquals(
      mutableListOf("No documentation for: https://api1.blah.com/me"),
      testOutputHandler.stdout
    )
  }
}
