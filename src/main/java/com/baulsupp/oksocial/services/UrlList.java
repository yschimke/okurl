package com.baulsupp.oksocial.services;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class UrlList {
  private List<String> urls;

  public UrlList(List<String> urls) {
    this.urls = urls;
  }

  public List<String> getUrls() {
    return urls;
  }

  public static UrlList fromResource(String serviceName) throws IOException {
    URL url = UrlList.class.getResource("/urls/" + serviceName + ".txt");
    if (url != null) {
      return new UrlList(Resources.readLines(url, StandardCharsets.UTF_8));
    } else {
      return new UrlList(Collections.emptyList());
    }
  }

  public List<String> matchingUrls(String prefix) {
    return urls.stream().filter(u -> u.startsWith(prefix)).collect(toList());
  }
}
