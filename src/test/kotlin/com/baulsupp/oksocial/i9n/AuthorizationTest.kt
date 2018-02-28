package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
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

    main.run()

    assertEquals("abc", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorize() {
    main.authorize = true
    main.arguments = mutableListOf("test")

    main.run()

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorizeByHost() {
    main.authorize = true
    main.arguments = mutableListOf("https://test.com/test")

    main.run()

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  fun authorizeWithArgs() {
    main.authorize = true
    main.arguments = mutableListOf("test", "TOKENARG")

    main.run()

    assertEquals("TOKENARG", credentialsStore.tokens["localhost"])
  }
}
