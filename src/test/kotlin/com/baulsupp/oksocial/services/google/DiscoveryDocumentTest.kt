package com.baulsupp.oksocial.services.google

import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class DiscoveryDocumentTest {
  private var doc: DiscoveryDocument? = null

  @Before
  @Throws(IOException::class)
  fun loadStaticIndex() {
    doc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("urlshortener.json").readText())
  }

  @Test
  @Throws(IOException::class)
  fun getUrlsFromFile() {
    assertEquals("https://www.googleapis.com/urlshortener/v1/", doc!!.baseUrl)

    assertEquals(listOf("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"), doc!!.urls)
  }

  @Test
  @Throws(IOException::class)
  fun loadGmail() {
    val gmailDoc = DiscoveryDocument.parse(DiscoveryDocumentTest::class.java.getResource("gmail.json").readText())

    val endpoints = gmailDoc.endpoints

    for (s in endpoints) {
      println(s.url())
    }
  }
}
