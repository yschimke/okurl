package com.baulsupp.okurl.services.google

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiscoveryDocumentTest {
  @Test
  fun loadGmail() {
    val gmailDoc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("gmail.json").readText())

    val endpoints = gmailDoc.endpoints

    for (s in endpoints) {
      println(s.url())
    }
  }

  @Test
  fun loadUrlShortener() {
    val doc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("urlshortener.json").readText())

    val endpoints = doc.endpoints

    for (s in endpoints) {
      println(s.url())
    }

    assertEquals("https://www.googleapis.com/urlshortener/v1/", doc.baseUrl)

    assertEquals(listOf("https://www.googleapis.com/urlshortener/v1/url",
      "https://www.googleapis.com/urlshortener/v1/url/history"), doc.urls)
  }
}
