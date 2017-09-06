package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.services.twitter.twurlrc.TwurlCredentialsStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

class TwurlCredentialsStoreTest {
  @Test
  @Throws(IOException::class)
  fun testReadDefaultCredentials() {
    val file = File(TwurlCredentialsStoreTest::class.java.getResource("/single_twurlrc.yaml").file)
    val store = TwurlCredentialsStore(file)

    val credentials = store.readCredentials().get()

    assertEquals("PROFILE", credentials.username)
    assertEquals("CONSUMER_KEY", credentials.consumerKey)
    assertEquals("CONSUMER_SECRET", credentials.consumerSecret)
    assertEquals("1234-TOKEN", credentials.token)
    assertEquals("SECRET", credentials.secret)
  }
}
