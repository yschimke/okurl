package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.DefaultToken
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.services.test.TestAuthInterceptor
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrlCompleterTest {
  private val services = listOf<AuthInterceptor<*>>(TestAuthInterceptor())

  private val completer = UrlCompleter(services, OkHttpClient(), CredentialsStore.NONE,
          CompletionVariableCache.NONE)

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
