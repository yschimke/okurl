package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImgurTest {
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
    main.arguments = mutableListOf("imgur")

    runBlocking {
      main.run()
    }

    assertEquals("abc", credentialsStore.tokens["api.imgur.com"])
  }
}
