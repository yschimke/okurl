package com.baulsupp.oksocial.services;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import okhttp3.HttpUrl;

import static java.util.stream.Collectors.toList;

public class UrlCompleter {
  private Iterable<AuthInterceptor<?>> services;
  private CredentialsStore credentialsStore;

  public UrlCompleter(Iterable<AuthInterceptor<?>> services, CredentialsStore credentialsStore) {
    this.services = services;
    this.credentialsStore = credentialsStore;
  }

  public List<String> urlList(String prefix) throws IOException {
    if (prefix.matches("https://.*/.*")) {
      return endpointUrls(prefix);
    } else {
      return topLevelUrls(prefix);
    }
  }

  private List<String> endpointUrls(String prefix) throws IOException {
    HttpUrl url = HttpUrl.parse(prefix);

    for (AuthInterceptor<?> a : services) {
      if (a.supportsUrl(url)) {
        return a.matchingUrls(prefix, credentialsStore);
      }
    }

    return Lists.newArrayList();
  }

  private List<String> topLevelUrls(String prefix) {
    List<String> list = new ArrayList<>(100);

    for (AuthInterceptor<?> a : services) {
      list.addAll(a.hosts());
    }

    list.sort(Comparator.naturalOrder());

    return list.stream()
        .map(h -> "https://" + h + "/")
        .filter(u -> u.startsWith(prefix))
        .collect(toList());
  }
}
