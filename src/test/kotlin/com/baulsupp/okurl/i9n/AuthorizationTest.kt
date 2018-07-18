package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthorizationTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  fun setToken() {
    main.authorize = true
    main.token = "abc"
    main.arguments = mutableListOf("test")

    runBlocking {
      main.run()
    }

    assertEquals("abc", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorize() {
    main.authorize = true
    main.arguments = mutableListOf("test")

    runBlocking {
      main.run()
    }

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorizeByHost() {
    main.authorize = true
    main.arguments = mutableListOf("https://test.com/test")

    runBlocking {
      main.run()
    }

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorizeWithArgs() {
    main.authorize = true
    main.arguments = mutableListOf("test", "TOKENARG")

    runBlocking {
      main.run()
    }

    assertEquals("TOKENARG", credentialsStore.tokens["localhost"])
  }
}
