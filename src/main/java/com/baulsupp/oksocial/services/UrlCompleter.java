package com.baulsupp.oksocial.services;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static java.util.stream.Collectors.toList;

public class UrlCompleter {
  private Iterable<AuthInterceptor<?>> services;
  private OkHttpClient client;
  private CredentialsStore credentialsStore;

  public UrlCompleter(Iterable<AuthInterceptor<?>> services, OkHttpClient client,
      CredentialsStore credentialsStore) {
    this.services = services;
    this.client = client;
    this.credentialsStore = credentialsStore;
  }

  public List<String> urlList(String prefix) throws IOException {
      return endpointUrls(prefix);
  }

  private List<String> endpointUrls(String prefix) throws IOException {
    List<String> results = Lists.newArrayList();

    for (AuthInterceptor<?> a : services) {
      results.addAll(a.matchingUrls(prefix, client, credentialsStore));
    }

    return results;
  }
}
