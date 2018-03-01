package com.baulsupp.oksocial.services.google

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoveryIndexTest {
  @Test

  fun loadStatic() {
    val r = DiscoveryIndex.loadStatic()

    assertEquals(
        listOf("https://www.googleapis.com/discovery/v1/apis/urlshortener/v1/rest"),
        r.getDiscoveryUrlForApi("https://www.googleapis.com/urlshortener/v1/"))
  }

  @Test

  fun getsUniqueResult() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://people.googleapis.com/xxx")

    assertEquals(listOf("https://www.googleapis.com/discovery/v1/apis/people/v1/rest"),
        results)
  }

  @Test

  fun mergesAllResultsForLongPrefix() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://www.googleapis.com/p")

    assertTrue(results.size > 5)

    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"))
    assertFalse(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"))
  }

  @Test

  fun mergesAllResults() {
    val r = DiscoveryIndex.loadStatic()

    val results = r.getDiscoveryUrlForPrefix("https://www.googleapis.co")

    assertTrue(results.size > 5)
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"))
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"))
  }
}
