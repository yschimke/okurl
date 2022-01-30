package com.baulsupp.okurl.i9n

import com.baulsupp.schoutput.handler.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.okhttp.localhost
import com.baulsupp.okurl.security.CertificatePin
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Disabled
class WebServerTest {
  @JvmField
  var server = MockWebServer()

  private val main = Main()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
  }

  private val sslClient = localhost()

  @Test
  fun httpsRequestInsecureFails() {
    server.useHttps(sslClient.sslSocketFactory(), false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = mutableListOf(server.url("/").toString())

    runBlocking {
      main.run()
    }

    assertEquals(0, output.responses.size)
    assertEquals(1, output.failures.size)
    assertTrue(output.failures[0] is SSLHandshakeException)
  }

  @Test
  fun httpsRequestInsecure() {
    server.useHttps(sslClient.sslSocketFactory(), false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = mutableListOf(server.url("/").toString())
    main.insecureHost = listOf(server.hostName)

    runBlocking {
      main.run()
    }

    assertEquals(1, output.responses.size)
    assertEquals(200, output.responses[0].code)
  }

  @Test
  @Disabled
  fun httpsRequestSecure() {
    server.useHttps(sslClient.sslSocketFactory(), false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = mutableListOf(server.url("/").toString())

    runBlocking {
      main.run()
    }

    assertEquals(1, output.responses.size)
    assertEquals(200, output.responses[0].code)
  }

  @Test
  fun rejectedWithPin() {
    server.useHttps(sslClient.sslSocketFactory(), false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = mutableListOf(server.url("/").toString())
    main.certificatePins = listOf(CertificatePin(server.hostName + ":" +
      "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="))
    main.insecureHost = listOf(server.hostName)

    runBlocking {
      main.run()
    }

    assertEquals(0, output.responses.size)
    assertEquals(1, output.failures.size)
    assertTrue(output.failures[0] is SSLPeerUnverifiedException)
  }
}
