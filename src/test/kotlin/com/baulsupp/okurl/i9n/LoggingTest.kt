package com.baulsupp.okurl.i9n

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.okhttp.localhost
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.util.logging.LogManager
import kotlin.test.assertEquals

class LoggingTest {
  @Rule
  @JvmField
  var server = MockWebServer()

  private val main = Main()

  private val sslClient = localhost()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
  }

  @Test
  fun logsData() {
    server.useHttps(sslClient.sslSocketFactory(), false)
    server.run { server.protocols = listOf(Protocol.HTTP_2, Protocol.HTTP_1_1) }
    server.enqueue(MockResponse().setBody("Isla Sorna"))
    main.insecureHost = listOf(server.hostName)

    main.arguments = mutableListOf(server.url("/").toString())
    main.debug = true

    runBlocking {
      main.run()
    }
  }

  @Test
  fun version() {
    val output = TestOutputHandler<Response>()

    main.complete = "protocol"

    runBlocking {
      main.run()
    }

    assertEquals(0, output.failures.size)
  }

  companion object {
    @AfterClass
    fun resetLogging() {
      LogManager.getLogManager().reset()
    }
  }
}
