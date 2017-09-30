package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.internal.tls.SslClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test


import java.util.logging.LogManager
import kotlin.test.assertEquals

class LoggingTest {
    @Rule @JvmField
    var server = MockWebServer()

    private val main = Main()

    private val sslClient = SslClient.localhost()
    private val output = TestOutputHandler<Response>()

    init {
        main.outputHandler = output
    }

    @Test
    @Throws(Exception::class)
    fun logsData() {
        server.useHttps(sslClient.socketFactory, false)
        server.setProtocols(Lists.newArrayList(Protocol.HTTP_2, Protocol.HTTP_1_1))
        server.enqueue(MockResponse().setBody("Isla Sorna"))
        main.allowInsecure = true

        main.arguments = Lists.newArrayList(server.url("/").toString())
        main.debug = true

        main.run()
    }

    @Test
    @Throws(Exception::class)
    fun version() {
        val output = TestOutputHandler<Response>()

        main.version = true

        main.run()

        assertEquals(0, output.failures.size)
    }

    companion object {

        @AfterClass
        fun resetLogging() {
            LogManager.getLogManager().reset()
        }
    }
}
