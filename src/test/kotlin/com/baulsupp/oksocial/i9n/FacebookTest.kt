package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.facebook.FacebookApiDocPresenter
import com.baulsupp.oksocial.services.facebook.FacebookAuthInterceptor
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class FacebookTest {
    private val main = Main()
    private val output = TestOutputHandler<Response>()

    init {
        main.outputHandler = output
        main.credentialsStore = TestCredentialsStore()
    }

    private val sd = FacebookAuthInterceptor().serviceDefinition()
    private var p: FacebookApiDocPresenter? = null

    @BeforeEach
    @Throws(IOException::class)
    fun loadPresenter() {
        p = FacebookApiDocPresenter(sd)
    }

    @Test
    @Throws(IOException::class)
    fun testExplainsUrl() {
        assumeHasNetwork()

        main.arguments = Lists.newArrayList("https://graph.facebook.com/v2.10/app/groups")
        main.apiDoc = true

        main.run()

        val es = newArrayList("service: facebook", "name: Facebook API",
                "docs: https://developers.facebook.com/docs/graph-api",
                "apps: https://developers.facebook.com/apps/")

        assertEquals(es, output.stdout)
    }
}
