package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.internal.tls.SslClient
import org.junit.jupiter.api.AfterAll
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.util.logging.LogManager
import kotlin.test.assertEquals

class LoggingTest {
  @Rule
  @JvmField
  var server = MockWebServer()

  private val main = Main()

  private val sslClient = SslClient.localhost()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
  }

  @Test
  fun logsData() {
    server.useHttps(sslClient.socketFactory, false)
    server.setProtocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
    server.enqueue(MockResponse().setBody("Isla Sorna"))
    main.allowInsecure = true

    main.arguments = mutableListOf(server.url("/").toString())
    main.debug = true

    main.run()
  }

  @Test
  fun version() {
    val output = TestOutputHandler<Response>()

    main.version = true

    main.run()

    assertEquals(0, output.failures.size)
  }

  companion object {

    @AfterAll
    fun resetLogging() {
      LogManager.getLogManager().reset()
    }
  }
}
