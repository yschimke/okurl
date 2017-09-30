package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class HttpsCompletion {
    private val main = Main()
    private val output = TestOutputHandler<Response>()
    private val credentialsStore = TestCredentialsStore()

    init {
        main.outputHandler = output
        main.credentialsStore = credentialsStore
    }

    @Test
    @Throws(Throwable::class)
    fun completePeopleEndpointSite() {
        main.arguments = newArrayList("https://")
        main.urlComplete = true

        main.run()

        assertEquals(mutableListOf(), output.failures)
        assertTrue(output.stdout[0].contains("https://people.googleapis.com/"))
        assertTrue(output.stdout[0].contains("https://graph.facebook.com/"))
    }
}
