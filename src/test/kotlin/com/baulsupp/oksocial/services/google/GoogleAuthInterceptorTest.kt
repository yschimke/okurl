package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.i9n.TestCredentialsStore
import com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork
import com.google.common.collect.Lists
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.ExecutionException

class GoogleAuthInterceptorTest {
  private val interceptor = GoogleAuthInterceptor()
  private val client = OkHttpClient()
  private val cache = CompletionVariableCache.NONE
  private val credentialsStore = TestCredentialsStore()

  @Test
  @Throws(IOException::class)
  fun hasManyHosts() {
    assertTrue(interceptor.hosts().size > 10)
    assertTrue(interceptor.hosts().contains("www.googleapis.com"))
    assertTrue(interceptor.hosts().contains("people.googleapis.com"))
  }

  @Test
  @Throws(IOException::class)
  fun supportsAnyGoogleApi() {
    assertTrue(interceptor.supportsUrl(HttpUrl.parse("https://altrightfanfiction.googleapis.com")))
  }

  @Test
  @Throws(IOException::class, ExecutionException::class, InterruptedException::class)
  fun completesHosts() {
    val hostCompleter = interceptor.apiCompleter("https://", client, credentialsStore, cache)

    val urls = hostCompleter.prefixUrls().get().getUrls("https://")

    assertTrue(urls.contains("https://www.googleapis.com"))
    assertTrue(urls.contains("https://people.googleapis.com"))
  }

  @Test
  @Throws(IOException::class, ExecutionException::class, InterruptedException::class)
  fun completesWwwPaths() {
    val hostCompleter = interceptor.apiCompleter("https://people.googleapis.com", client,
        credentialsStore, cache)

    val urls = hostCompleter.siteUrls(HttpUrl.parse("https://people.googleapis.com"))
        .get()
        .getUrls("https://people.googleapis.com")

    assertEquals(Lists.newArrayList("https://people.googleapis.com/"), urls)
  }

  // hits the network
  @Test
  @Throws(IOException::class, ExecutionException::class, InterruptedException::class)
  fun completesSitePaths() {
    assumeHasNetwork()

    val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/urlshortener/v1/url",
        client,
        credentialsStore, cache)

    val urlList = hostCompleter.siteUrls(
        HttpUrl.parse("https://www.googleapis.com/urlshortener/v1/url"))
        .get()

    val urls = urlList
        .getUrls("https://www.googleapis.com/urlshortener/v1/url")

    assertEquals(Lists.newArrayList("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"), urls)
  }

  // hits the network
  @Test
  @Throws(IOException::class, ExecutionException::class, InterruptedException::class)
  fun completesSitePathsForDuplicates() {
    assumeHasNetwork()

    val hostCompleter = interceptor.apiCompleter("https://www.googleapis.com/", client,
        credentialsStore, cache)

    val urlList = hostCompleter.siteUrls(HttpUrl.parse("https://www.googleapis.com/"))
        .get()

    val urls = urlList
        .getUrls("https://www.googleapis.com/")

    assertTrue(urls.size > 5)

    assertTrue(urls.contains("https://www.googleapis.com/urlshortener/v1/url/history"))
    assertTrue(urls.contains("https://www.googleapis.com/oauth2/v1/userinfo"))
  }
}
