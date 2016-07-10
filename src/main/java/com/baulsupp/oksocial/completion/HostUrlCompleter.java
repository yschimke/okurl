package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import okhttp3.HttpUrl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

public class HostUrlCompleter implements ApiCompleter {
  private final Collection<String> hosts;

  public HostUrlCompleter(Collection<String> hosts) {
    this.hosts = hosts;
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    return completedFuture(
        new UrlList(UrlList.Match.SITE, urls()));
  }

  private List<String> urls() {
    return hosts.stream()
        .flatMap(h -> Stream.of("https://" + h, "https://" + h + "/"))
        .collect(toList());
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    return completedFuture(
        new UrlList(UrlList.Match.HOSTS, urls()));
  }
}
