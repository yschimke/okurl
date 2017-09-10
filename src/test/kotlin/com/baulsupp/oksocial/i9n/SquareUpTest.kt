package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.squareup.SquareUpAuthInterceptor
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class SquareUpTest {

    private val main = Main()
    private val output = TestOutputHandler<Response>()
    private val completionCache = TestCompletionVariableCache()
    private val credentialsStore = TestCredentialsStore()

    init {
        main.outputHandler = output
        main.completionVariableCache = completionCache
        main.credentialsStore = credentialsStore
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointWithReplacements() {
        main.arguments = newArrayList("https://connect.squareup.com/")
        main.urlComplete = true
        completionCache.store("squareup", "locations", Lists.newArrayList("AA", "bb"))
        credentialsStore.storeCredentials(Oauth2Token(""),
                SquareUpAuthInterceptor().serviceDefinition())

        main.run()

        assertEquals(listOf(), output.failures)
        assertEquals(1, output.stdout.size)
        assertTrue(output.stdout[0].contains("/v2/locations/AA/transactions"))
    }
}
