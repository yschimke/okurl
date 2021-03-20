package com.baulsupp.okurl.completion

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrlListTest {
  @Test
  fun testReplacements() {
    val l = UrlList(UrlList.Match.EXACT,
        listOf("https://a.com/{location}", "https://a.com/here"))

    assertEquals(listOf("https://a.com/A", "https://a.com/B", "https://a.com/{location}",
        "https://a.com/here"), l.replace("location", listOf("A", "B"), true).getUrls(""))
  }

  @Test
  fun testReplacementsEmpty() {
    val l = UrlList(UrlList.Match.EXACT,
        listOf("https://a.com/{location}", "https://a.com/here"))

    assertEquals(listOf("https://a.com/{location}", "https://a.com/here"),
        l.replace("location", listOf(), true).getUrls(""))
  }
}
