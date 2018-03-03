package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.DefaultToken
import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.AuthenticatingInterceptor
import com.baulsupp.oksocial.services.test.TestAuthInterceptor
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrlCompleterTest {
  private val main = Main()

  init {
    main.authenticatingInterceptor = AuthenticatingInterceptor(main, listOf(TestAuthInterceptor()))
    main.initialise()
  }

  private val completer = UrlCompleter(main)

  @Test
  fun returnsAllUrls() {
    runBlocking {
      assertEquals(
              UrlList(UrlList.Match.HOSTS,
                      listOf("https://test.com", "https://test.com/",
                              "https://api1.test.com",
                              "https://api1.test.com/")),
              completer.urlList("", DefaultToken))
    }
  }

  @Test
  fun returnsMatchingUrls() {
    runBlocking {
      assertEquals(
              listOf("https://api1.test.com", "https://api1.test.com/"),
              completer.urlList("https://api1", DefaultToken).getUrls("https://api1"))
      assertEquals(listOf(),
              completer.urlList("https://api2", DefaultToken).getUrls("https://api2"))
    }
  }

  @Test
  fun returnsMatchingEndpointUrls() {
    runBlocking {
      assertEquals(listOf("https://api1.test.com/users.json",
              "https://api1.test.com/usersList.json"),
              completer.urlList("https://api1.test.com/u", DefaultToken).getUrls("https://api1.test.com/u"))
    }
  }
}
