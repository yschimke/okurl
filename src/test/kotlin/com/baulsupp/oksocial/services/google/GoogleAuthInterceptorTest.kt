package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.i9n.TestCredentialsStore
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleAuthInterceptorTest {
  private val interceptor = GoogleAuthInterceptor()
  private val client = OkHttpClient()
  private val cache = CompletionVariableCache.NONE
  private val credentialsStore = TestCredentialsStore()

  @Test

  fun hasManyHosts() {
    assertTrue(interceptor.hosts().size > 10)
    assertTrue(interceptor.hosts().contains("www.googleapis.com"))
    assertTrue(interceptor.hosts().contains("people.googleapis.com"))
  }

  @Test

  fun supportsAnyGoogleApi() {
    assertTrue(interceptor.supportsUrl(HttpUrl.parse("https://altrightfanfiction.googleapis.com")!!))
  }

  @Test
  fun completesHosts() {
    runBlocking {
      val hostCompleter = interceptor.apiCompleter("https://", client, credentialsStore, cache, null)

      val urls = hostCompleter.prefixUrls().getUrls("https://")

      assertTrue(urls.contains("https://www.googleapis.com"))
      assertTrue(urls.contains("https://people.googleapis.com"))
    }
  }

  @Test
  fun completesWwwPaths() {
    runBlocking {
      val hostCompleter = interceptor.apiCompleter("https://people.googleapis.com", client,
        credentialsStore, cache, null)

      val urls = hostCompleter.siteUrls(HttpUrl.parse("https://people.googleapis.com")!!)
              .getUrls("https://people.googleapis.com")

      assertEquals(listOf("https://people.googleapis.com/"), urls)
    }
  }

  // hits the network
  @Test
  fun completesSitePaths() {
    runBlocking {
      assumeHasNetwork()

      val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/urlshortener/v1/url",
        client,
        credentialsStore, cache, null)

      val urlList = hostCompleter.siteUrls(
              HttpUrl.parse("https://www.googleapis.com/urlshortener/v1/url")!!)

      val urls = urlList
              .getUrls("https://www.googleapis.com/urlshortener/v1/url")

      assertEquals(listOf("https://www.googleapis.com/urlshortener/v1/url",
              "https://www.googleapis.com/urlshortener/v1/url/history"), urls)
    }
  }

  // hits the network
  @Test
  fun completesSitePathsForDuplicates() {
    runBlocking {
      assumeHasNetwork()

      val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/", client,
        credentialsStore, cache, null)

      val urlList = hostCompleter.siteUrls(HttpUrl.parse("https://www.googleapis.com/")!!)

      val urls = urlList
              .getUrls("https://www.googleapis.com/")

      assertTrue(urls.size > 5)

      assertTrue(urls.contains("https://www.googleapis.com/urlshortener/v1/url/history"))
      assertTrue(urls.contains("https://www.googleapis.com/oauth2/v1/userinfo"))
    }
  }
}
