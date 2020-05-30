package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.services.twitter.twurlrc.TwurlCredentialsStore
import org.junit.Test

import java.io.File
import kotlin.test.assertEquals

class TwurlCredentialsStoreTest {
  @Test
  fun testReadDefaultCredentials() {
    val file = File(TwurlCredentialsStoreTest::class.java.getResource("/single_twurlrc.yaml").file)
    val store = TwurlCredentialsStore(file)

    val credentials = store.readCredentials()!!

    assertEquals("PROFILE", credentials.username)
    assertEquals("CONSUMER_KEY", credentials.consumerKey)
    assertEquals("CONSUMER_SECRET", credentials.consumerSecret)
    assertEquals("1234-TOKEN", credentials.token)
    assertEquals("SECRET", credentials.secret)
  }
}
