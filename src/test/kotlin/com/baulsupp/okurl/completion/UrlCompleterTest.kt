package com.baulsupp.okurl.completion

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.i9n.TestCredentialsStore
import com.baulsupp.okurl.services.test.TestAuthInterceptor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class UrlCompleterTest {
  private val main = Main().apply {
    credentialsStore = TestCredentialsStore()
    authenticatingInterceptor = AuthenticatingInterceptor(credentialsStore, mutableListOf(TestAuthInterceptor()))
    initialise()
  }

  private val completer = UrlCompleter(main)

  @Test
  fun returnsAllUrls() {
    runBlocking {
      assertEquals(
        UrlList(
          UrlList.Match.HOSTS,
          listOf(
            "https://test.com",
            "https://test.com/",
            "https://api1.test.com",
            "https://api1.test.com/"
          )
        ),
        completer.urlList("", DefaultToken)
      )
    }
  }

  @Test
  fun returnsMatchingUrls() {
    runBlocking {
      assertEquals(
        listOf("https://api1.test.com", "https://api1.test.com/"),
        completer.urlList("https://api1", DefaultToken).getUrls("https://api1")
      )
      assertEquals(
        listOf(),
        completer.urlList("https://api2", DefaultToken).getUrls("https://api2")
      )
    }
  }

  @Test
  fun returnsMatchingEndpointUrls() {
    runBlocking {
      assertEquals(
        listOf(
          "https://api1.test.com/users.json",
          "https://api1.test.com/usersList.json"
        ),
        completer.urlList(
          "https://api1.test.com/u",
          DefaultToken
        ).getUrls("https://api1.test.com/u")
      )
    }
  }
}
