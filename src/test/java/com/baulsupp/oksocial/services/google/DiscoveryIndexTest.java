package com.baulsupp.oksocial.services.google;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscoveryIndexTest {
  @Test public void loadStatic() throws IOException {
    DiscoveryIndex r = DiscoveryIndex.loadStatic();

    assertEquals(
        newArrayList("https://www.googleapis.com/discovery/v1/apis/urlshortener/v1/rest"),
        r.getDiscoveryUrlForApi("https://www.googleapis.com/urlshortener/v1/"));
  }

  @Test public void getsUniqueResult() throws IOException {
    DiscoveryIndex r = DiscoveryIndex.loadStatic();

    List<String> results = r.getDiscoveryUrlForPrefix("https://people.googleapis.com/xxx");

    assertEquals(newArrayList("https://www.googleapis.com/discovery/v1/apis/people/v1/rest"),
        results);
  }

  @Test public void mergesAllResultsForLongPrefix() throws IOException {
    DiscoveryIndex r = DiscoveryIndex.loadStatic();

    List<String> results = r.getDiscoveryUrlForPrefix("https://www.googleapis.com/p");

    assertTrue(results.size() > 5);

    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"));
    assertFalse(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"));
  }

  @Test public void mergesAllResults() throws IOException {
    DiscoveryIndex r = DiscoveryIndex.loadStatic();

    List<String> results = r.getDiscoveryUrlForPrefix("https://www.googleapis.co");

    assertTrue(results.size() > 5);
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/plus/v1/rest"));
    assertTrue(results.contains("https://www.googleapis.com/discovery/v1/apis/games/v1/rest"));
  }
}