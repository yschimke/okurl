package com.baulsupp.oksocial.services.google

import com.google.common.collect.Lists.newArrayList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException

class DiscoveryIndexTest {
  @Test
  @Throws(IOException::class)
  fun loadStatic() {
    val r = DiscoveryIndex.loadStatic()

    assertEquals(
        newArrayList("https://www.googleapis.com/discovery/v1/apis/urlshortener/v1/rest"),
        r.getDiscoveryUrlForApi("https://www.googleapis.com/urlshortener/v1/"))
  }

  @Test
  @Throws(IOException::class)
  fun getsUniqueResult() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://people.googleapis.com/xxx")

    assertEquals(newArrayList("https://www.googleapis.com/discovery/v1/apis/people/v1/rest"),
        results)
  }

  @Test
  @Throws(IOException::class)
  fun mergesAllResultsForLongPrefix() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://www.googleapis.com/p")

    assertTrue(results.size > 5)

    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"))
    assertFalse(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"))
  }

  @Test
  @Throws(IOException::class)
  fun mergesAllResults() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://www.googleapis.co")

    assertTrue(results.size > 5)
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"))
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"))
  }
}