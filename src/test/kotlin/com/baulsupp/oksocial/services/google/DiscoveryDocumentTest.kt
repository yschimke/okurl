package com.baulsupp.oksocial.services.google

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiscoveryDocumentTest {
  private var doc: DiscoveryDocument? = null

  @BeforeEach

  fun loadStaticIndex() {
    doc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("urlshortener.json").readText())
  }

  @Test

  fun getUrlsFromFile() {
    assertEquals("https://www.googleapis.com/urlshortener/v1/", doc!!.baseUrl)

    assertEquals(listOf("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"), doc!!.urls)
  }

  @Test

  fun loadGmail() {
    val gmailDoc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("gmail.json").readText())

    val endpoints = gmailDoc.endpoints

    for (s in endpoints) {
      println(s.url())
    }
  }
}
