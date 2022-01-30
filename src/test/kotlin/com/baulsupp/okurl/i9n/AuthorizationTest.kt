package com.baulsupp.okurl.i9n

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.services.test.TestAuthInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthorizationTest {
  private val output = TestOutputHandler<Response>()
  private val store = TestCredentialsStore()
  private val main = Main().apply {
    outputHandler = output
    credentialsStore = store
    authenticatingInterceptor = AuthenticatingInterceptor(credentialsStore, mutableListOf(TestAuthInterceptor()))
  }

  @Test
  fun setToken() {
    main.authorize = "test"
    main.token = "abc"

    runBlocking {
      main.run()
    }

    assertEquals("abc", store.tokens[Pair("test", DefaultToken.name)])
  }

  @Test
  fun authorize() {
    main.authorize = "test"

    runBlocking {
      main.run()
    }

    assertEquals("testToken", store.tokens[Pair("test", DefaultToken.name)])
  }

  @Test
  fun authorizeWithArgs() {
    main.authorize = "test"
    main.arguments = mutableListOf("TOKENARG")

    runBlocking {
      main.run()
    }

    assertEquals("TOKENARG", store.tokens[Pair("test", DefaultToken.name)])
  }
}
