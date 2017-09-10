package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.github.GithubAuthInterceptor
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GithubTest {
    private val main = Main()
    private val output = TestOutputHandler<Response>()
    private val credentialsStore = TestCredentialsStore()
    private val service = GithubAuthInterceptor().serviceDefinition()

    init {
        main.outputHandler = output
        main.credentialsStore = credentialsStore
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpointShortCommand1() {
        credentialsStore.storeCredentials(Oauth2Token("ABC"), service)

        main.commandName = "okapi"
        main.arguments = newArrayList("commands/githubapi", "/")
        main.urlComplete = true

        main.run()

        assertEquals(newArrayList<Any>(), output.failures)
        assertTrue(output.stdout[0].contains("/user"))
    }

    @Test
    @Throws(Throwable::class)
    fun completeEndpoint() {
        credentialsStore.storeCredentials(Oauth2Token("ABC"), service)

        main.arguments = newArrayList("https://api.github.com/")
        main.urlComplete = true

        main.run()

        assertEquals(newArrayList<Any>(), output.failures)
        assertTrue(output.stdout[0].contains("https://api.github.com/user"))
    }
}
