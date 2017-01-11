package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static com.baulsupp.oksocial.util.FutureUtil.join;
import static java.util.stream.Collectors.toList;

public class GoogleDiscoveryCompleter implements ApiCompleter {
  private OkHttpClient client;
  private DiscoveryRegistry discoveryRegistry;
  private final List<String> discoveryDocPaths;

  public GoogleDiscoveryCompleter(OkHttpClient client, DiscoveryRegistry discoveryRegistry,
      List<String> discoveryDocPaths) {
    this.client = client;
    this.discoveryRegistry = discoveryRegistry;
    this.discoveryDocPaths = discoveryDocPaths;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    // not supported for partial urls
    throw new UnsupportedOperationException();
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    List<CompletableFuture<List<String>>> futures =
        discoveryDocPaths.stream().map(this::singleFuture).collect(toList());

    return join(futures).thenApply(this::flattenList);
  }

  private UrlList flattenList(List<List<String>> l) {
    return new UrlList(UrlList.Match.SITE, l.stream().flatMap(List::stream).collect(toList()));
  }

  private CompletableFuture<List<String>> singleFuture(String discoveryDocPath) {
    return discoveryRegistry.load(client, discoveryDocPath).thenApply(s -> s.getUrls());
  }

  public static GoogleDiscoveryCompleter forApis(OkHttpClient client,
      DiscoveryRegistry discoveryRegistry,
      List<String> discoveryDocPaths) {
    return new GoogleDiscoveryCompleter(client, discoveryRegistry, discoveryDocPaths);
  }
}
