package com.baulsupp.oksocial.services.google

import com.google.common.io.Resources
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class DiscoveryDocumentTest {
  private var doc: DiscoveryDocument? = null

  @Before
  @Throws(IOException::class)
  fun loadStaticIndex() {
    val url = DiscoveryDocumentTest::class.java.getResource("urlshortener.json")

    val definition = Resources.toString(url, StandardCharsets.UTF_8)

    doc = DiscoveryDocument.parse(definition)
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
    val url = DiscoveryDocumentTest::class.java.getResource("gmail.json")

    val definition = Resources.toString(url, StandardCharsets.UTF_8)

    val gmailDoc = DiscoveryDocument.parse(definition)

    val endpoints = gmailDoc.endpoints

    for (s in endpoints) {
      println(s.url())
    }
  }
}
