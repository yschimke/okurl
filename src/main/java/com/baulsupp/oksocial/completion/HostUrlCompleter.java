package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;
import okhttp3.HttpUrl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

public class HostUrlCompleter implements ApiCompleter {
  private final Collection<String> hosts;

  public HostUrlCompleter(Collection<String> hosts) {
    this.hosts = hosts;
  }

  @Override public CompletableFuture<UrlList> siteUrls(HttpUrl url) throws IOException {
    return completedFuture(
        new UrlList(UrlList.Match.SITE, urls(true)));
  }

  private List<String> urls(boolean siteOnly) {
    Function<String, Stream<String>> f;
    if (siteOnly) {
      f = h -> Stream.of("https://" + h + "/");
    } else {
      f = h -> Stream.of("https://" + h, "https://" + h + "/");
    }

    return hosts.stream().flatMap(f).collect(toList());
  }

  @Override public CompletableFuture<UrlList> prefixUrls() throws IOException {
    return completedFuture(
        new UrlList(UrlList.Match.HOSTS, urls(false)));
  }
}
