package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.security.CertificatePin
import com.google.common.collect.Lists
import okhttp3.Response
import okhttp3.internal.tls.SslClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Ignore
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

@ExtendWith(ExternalResourceSupport::class)
@Ignore // TODO work out why failing in CI
class WebServerTest {
  @Rule
  var server = MockWebServer()

  private val main = Main()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
  }

  private val sslClient = SslClient.localhost()

  @Test
  @Throws(Exception::class)
  fun httpsRequestInsecureFails() {
    server.useHttps(sslClient.socketFactory, false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = Lists.newArrayList(server.url("/").toString())

    main.run()

    assertEquals(0, output.responses.size)
    assertEquals(1, output.failures.size)
    assertTrue(output.failures[0] is SSLHandshakeException)
  }

  @Test
  @Throws(Exception::class)
  fun httpsRequestInsecure() {
    server.useHttps(sslClient.socketFactory, false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = Lists.newArrayList(server.url("/").toString())
    main.allowInsecure = true

    main.run()

    assertEquals(1, output.responses.size)
    assertEquals(200, output.responses[0].code())
  }

  @Test
  @Disabled
  @Throws(Exception::class)
  fun httpsRequestSecure() {
    server.useHttps(sslClient.socketFactory, false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = Lists.newArrayList(server.url("/").toString())

    main.run()

    assertEquals(1, output.responses.size)
    assertEquals(200, output.responses[0].code())
  }

  @Test
  @Throws(Exception::class)
  fun rejectedWithPin() {
    server.useHttps(sslClient.socketFactory, false)
    server.enqueue(MockResponse().setBody("Isla Sorna"))

    main.arguments = Lists.newArrayList(server.url("/").toString())
    main.certificatePins = Lists.newArrayList(CertificatePin(server.hostName + ":" +
        "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="))
    main.allowInsecure = true

    main.run()

    assertEquals(0, output.responses.size)
    assertEquals(1, output.failures.size)
    assertTrue(output.failures[0] is SSLPeerUnverifiedException)
  }
}
