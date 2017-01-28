package com.baulsupp.oksocial.services.google;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiscoveryDocumentTest {
  private DiscoveryDocument doc;

  @BeforeEach
  public void loadStaticIndex() throws IOException {
    URL url = DiscoveryDocumentTest.class.getResource("urlshortener.json");

    String definition = Resources.toString(url, StandardCharsets.UTF_8);

    doc = DiscoveryDocument.parse(definition);
  }

  @Test
  public void getUrlsFromFile() throws IOException {
    assertEquals("https://www.googleapis.com/urlshortener/v1/", doc.getBaseUrl());

    assertEquals(newArrayList("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"), doc.getUrls());
  }

  @Test
  public void loadGmail() throws IOException {
    URL url = DiscoveryDocumentTest.class.getResource("gmail.json");

    String definition = Resources.toString(url, StandardCharsets.UTF_8);

    DiscoveryDocument gmailDoc = DiscoveryDocument.parse(definition);

    List<DiscoveryEndpoint> endpoints = gmailDoc.getEndpoints();

    for (DiscoveryEndpoint s : endpoints) {
      System.out.println(s.url());
    }
  }
}
