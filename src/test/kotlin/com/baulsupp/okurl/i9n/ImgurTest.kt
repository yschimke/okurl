package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.credentials.DefaultToken
import kotlinx.coroutines.runBlocking
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
    main.authorize = "imgur"
    main.token = "abc"

    runBlocking {
      main.run()
    }

    assertEquals("abc", credentialsStore.tokens[Pair("imgur", DefaultToken.name)])
  }
}
