package com.baulsupp.oksocial.completion;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class UrlListTest {
  @Test public void testReplacements() {
    UrlList l = new UrlList(".*", newArrayList("https://a.com/{location}", "https://a.com/here"));

    assertEquals(newArrayList("https://a.com/A", "https://a.com/B", "https://a.com/{location}",
        "https://a.com/here"), l.replace("location", newArrayList("A", "B"), true).getUrls(""));
  }

  @Test public void testReplacementsEmpty() {
    UrlList l = new UrlList(".*", newArrayList("https://a.com/{location}", "https://a.com/here"));

    assertEquals(newArrayList("https://a.com/{location}", "https://a.com/here"),
        l.replace("location", newArrayList(), true).getUrls(""));
  }
}
