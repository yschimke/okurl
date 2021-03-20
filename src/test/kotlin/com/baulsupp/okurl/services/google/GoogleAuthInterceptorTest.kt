package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.i9n.TestCredentialsStore
import com.baulsupp.okurl.util.TestUtil.assumeHasNetwork
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
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
    assertTrue(interceptor.hosts(credentialsStore).size > 10)
    assertTrue(interceptor.hosts(credentialsStore).contains("www.googleapis.com"))
    assertTrue(interceptor.hosts(credentialsStore).contains("people.googleapis.com"))
  }

  @Test
  fun supportsAnyGoogleApi() {
    runBlocking {
      assertTrue(interceptor.supportsUrl(
        "https://altrightfanfiction.googleapis.com".toHttpUrlOrNull()!!, credentialsStore))
    }
  }

  @Test
  fun completesHosts() {
    runBlocking {
      val hostCompleter = interceptor.apiCompleter("https://", client, credentialsStore, cache,
        DefaultToken)

      val urls = hostCompleter.prefixUrls().getUrls("https://")

      assertTrue(urls.contains("https://www.googleapis.com"))
      assertTrue(urls.contains("https://people.googleapis.com"))
    }
  }

  @Test
  fun completesWwwPaths() {
    runBlocking {
      val hostCompleter = interceptor.apiCompleter("https://people.googleapis.com", client,
        credentialsStore, cache, DefaultToken)

      val urls = hostCompleter.siteUrls("https://people.googleapis.com".toHttpUrlOrNull()!!,
        DefaultToken)
              .getUrls("https://people.googleapis.com")

      assertEquals(listOf("https://people.googleapis.com/"), urls)
    }
  }

  // hits the com.baulsupp.okurl.network
  @Test
  @Disabled
  fun completesSitePaths() {
    runBlocking {
      assumeHasNetwork()

      val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/urlshortener/v1/url",
        client,
        credentialsStore, cache, DefaultToken)

      val urlList = hostCompleter.siteUrls(
              "https://www.googleapis.com/urlshortener/v1/url".toHttpUrlOrNull()!!,
        DefaultToken)

      val urls = urlList
              .getUrls("https://www.googleapis.com/urlshortener/v1/url")

      assertEquals(listOf("https://www.googleapis.com/urlshortener/v1/url",
              "https://www.googleapis.com/urlshortener/v1/url/history"), urls)
    }
  }

  // hits the com.baulsupp.okurl.network
  @Test
  @Disabled
  fun completesSitePathsForDuplicates() {
    runBlocking {
      assumeHasNetwork()

      val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/", client,
        credentialsStore, cache, DefaultToken)

      val urlList = hostCompleter.siteUrls("https://www.googleapis.com/".toHttpUrlOrNull()!!,
        DefaultToken)

      val urls = urlList
              .getUrls("https://www.googleapis.com/")

      assertTrue(urls.size > 5)

      assertTrue(urls.contains("https://www.googleapis.com/urlshortener/v1/url/history"))
      assertTrue(urls.contains("https://www.googleapis.com/oauth2/v1/userinfo"))
    }
  }
}
