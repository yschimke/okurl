package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.i9n.TestCredentialsStore
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class GoogleCompletionTest {
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
        assumeHasNetwork()

        main.arguments = newArrayList("https://people.googleapis.com/")
        main.urlComplete = true

        main.run()

        assertEquals(listOf(), output.failures)
        assertTrue(output.stdout[0].contains("https://people.googleapis.com/"))
    }

    @Test
    @Throws(Throwable::class)
    fun completePeopleEndpointPath() {
        assumeHasNetwork()

        main.arguments = newArrayList("https://people.googleapis.com/v1/people:batch")
        main.urlComplete = true

        main.run()

        assertEquals(listOf(), output.failures)
        assertTrue(output.stdout[0].contains("https://people.googleapis.com/v1/people:batchGet"))
    }

    @Test
    @Throws(Throwable::class)
    fun completeGmailUserId() {
        assumeHasNetwork()

        main.arguments = newArrayList("https://www.googleapis.com/gmail/v1/")
        main.urlComplete = true

        main.run()

        assertEquals(listOf(), output.failures)
        assertTrue(
                output.stdout[0].contains("https://www.googleapis.com/gmail/v1/users/me/profile"))
    }

    // Nested example
    @Test
    @Throws(Throwable::class)
    fun completeGmailMessages() {
        assumeHasNetwork()

        main.arguments = newArrayList("https://www.googleapis.com/gmail/v1/")
        main.urlComplete = true

        main.run()

        assertEquals(listOf(), output.failures)
        assertTrue(
                output.stdout[0].contains("https://www.googleapis.com/gmail/v1/users/me/messages"))
    }
}
