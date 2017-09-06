package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.services.test.TestAuthInterceptor
import com.google.common.collect.Lists
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

class UrlCompleterTest {
  private val services = Lists.newArrayList<AuthInterceptor<*>>(TestAuthInterceptor())

  private val completer = UrlCompleter(services, OkHttpClient(), CredentialsStore.NONE,
      CompletionVariableCache.NONE)

  @Test
  @Throws(IOException::class)
  fun returnsAllUrls() {
    assertEquals(
        UrlList(UrlList.Match.HOSTS,
            Lists.newArrayList("https://test.com", "https://test.com/", "https://api1.test.com",
                "https://api1.test.com/")),
        completer.urlList(""))
  }

  @Test
  @Throws(IOException::class)
  fun returnsMatchingUrls() {
    assertEquals(
        Lists.newArrayList("https://api1.test.com", "https://api1.test.com/"),
        completer.urlList("https://api1").getUrls("https://api1"))
    assertEquals(Lists.newArrayList<Any>(),
        completer.urlList("https://api2").getUrls("https://api2"))
  }

  @Test
  @Throws(IOException::class)
  fun returnsMatchingEndpointUrls() {
    assertEquals(Lists.newArrayList("https://api1.test.com/users.json",
        "https://api1.test.com/usersList.json"),
        completer.urlList("https://api1.test.com/u").getUrls("https://api1.test.com/u"))
  }
}
