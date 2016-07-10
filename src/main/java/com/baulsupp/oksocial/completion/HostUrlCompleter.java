package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;

public class HostUrlCompleter implements ApiCompleter {
  private final Collection<String> hosts;

  public HostUrlCompleter(Collection<String> hosts) {
    this.hosts = hosts;
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    return prefixUrls();
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    String regex = hosts.size() == 1 ? quote("https://" + hosts.iterator().next() + "/") : ".*";
    return completedFuture(new UrlList(regex, hosts.stream().map(h -> "https://" + h + "/")
        .collect(toList())));
  }
}
