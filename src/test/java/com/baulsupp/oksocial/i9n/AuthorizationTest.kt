package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthorizationTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()
  private val credentialsStore = TestCredentialsStore()

  init {
    main.outputHandler = output
    main.credentialsStore = credentialsStore
  }

  @Test
  @Throws(Exception::class)
  fun setToken() {
    main.authorize = true
    main.token = "abc"
    main.arguments = Lists.newArrayList("test")

    main.run()

    assertEquals("abc", credentialsStore.tokens["localhost"])
  }

  @Test
  @Throws(Exception::class)
  fun authorize() {
    main.authorize = true
    main.arguments = Lists.newArrayList("test")

    main.run()

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  @Throws(Exception::class)
  fun authorizeByHost() {
    main.authorize = true
    main.arguments = Lists.newArrayList("https://test.com/test")

    main.run()

    assertEquals("testToken", credentialsStore.tokens["localhost"])
  }

  @Test
  @Throws(Exception::class)
  fun authorizeWithArgs() {
    main.authorize = true
    main.arguments = Lists.newArrayList("test", "TOKENARG")

    main.run()

    assertEquals("TOKENARG", credentialsStore.tokens["localhost"])
  }
}
