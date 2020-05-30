package com.baulsupp.okurl.i9n

import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.services.facebook.FacebookApiDocPresenter
import com.baulsupp.okurl.services.facebook.FacebookAuthInterceptor
import com.baulsupp.okurl.services.facebook.VERSION
import com.baulsupp.okurl.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertEquals

class FacebookTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
    main.credentialsStore = TestCredentialsStore()
  }

  private val sd = FacebookAuthInterceptor().serviceDefinition
  private var p: FacebookApiDocPresenter? = FacebookApiDocPresenter(sd)

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
