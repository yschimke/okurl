package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Future;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

public class HostUrlCompleter implements ApiCompleter {
  private final Collection<String> hosts;

  public HostUrlCompleter(Collection<String> hosts) {
    this.hosts = hosts;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    return completedFuture(new UrlList(hosts.stream().map(h -> "https://" + h + "/")
        .collect(toList())));
  }
}
