package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.output.TestOutputHandler
import com.baulsupp.oksocial.services.facebook.FacebookApiDocPresenter
import com.baulsupp.oksocial.services.facebook.FacebookAuthInterceptor
import com.baulsupp.oksocial.services.facebook.FacebookUtil
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class FacebookTest {
  private val main = Main()
  private val output = TestOutputHandler<Response>()

  init {
    main.outputHandler = output
    main.credentialsStore = TestCredentialsStore()
  }

  private val sd = FacebookAuthInterceptor().serviceDefinition()
  private var p: FacebookApiDocPresenter? = null

  @Before
  @Throws(IOException::class)
  fun loadPresenter() {
    p = FacebookApiDocPresenter(sd)
  }

  @Test
  @Throws(IOException::class)
  fun testExplainsUrl() {
    assumeHasNetwork()

    main.arguments = mutableListOf("https://graph.facebook.com/${FacebookUtil.VERSION}/app/groups")
    main.apiDoc = true

    main.run()

    val es = listOf("service: facebook", "name: Facebook API",
        "docs: https://developers.facebook.com/docs/graph-api",
        "apps: https://developers.facebook.com/apps/")

    assertEquals(es, output.stdout)
  }
}
