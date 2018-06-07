package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.facebook.FacebookApiDocPresenter
import com.baulsupp.oksocial.services.facebook.FacebookAuthInterceptor
import com.baulsupp.oksocial.services.facebook.VERSION
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FacebookTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
    main.credentialsStore = TestCredentialsStore()
  }

  private val sd = FacebookAuthInterceptor().serviceDefinition
  private var p: FacebookApiDocPresenter? = null

  @BeforeEach
  fun loadPresenter() {
    p = FacebookApiDocPresenter(sd)
  }

  @Test
  fun testExplainsUrl() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://graph.facebook.com/$VERSION/app/groups")
    main.apiDoc = true

    runBlocking {
      main.run()
    }

    val es = listOf("service: facebook", "name: Facebook API",
        "docs: https://developers.facebook.com/docs/graph-api",
        "apps: https://developers.facebook.com/apps/")

    assertEquals(es, output.stdout)
  }
}
