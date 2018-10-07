package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.i9n.TestCredentialsStore
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TwitterAuthInterceptorTest {
  internal var auth = TwitterAuthInterceptor()

  @Test
  fun testHosts() {
    assertTrue(auth.hosts(TestCredentialsStore()).contains("api.twitter.com"))
  }
}
