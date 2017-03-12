package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.CompletionMappings;
import com.baulsupp.oksocial.completion.UrlList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import okhttp3.HttpUrl;

import static ee.schimke.oksocial.output.util.FutureUtil.join;
import static java.util.stream.Collectors.toList;

public class GoogleDiscoveryCompleter implements ApiCompleter {
  private DiscoveryRegistry discoveryRegistry;
  private final List<String> discoveryDocPaths;
  private CompletionMappings mappings = new CompletionMappings();

  public GoogleDiscoveryCompleter(DiscoveryRegistry discoveryRegistry,
      List<String> discoveryDocPaths) {
    this.discoveryRegistry = discoveryRegistry;
    this.discoveryDocPaths = discoveryDocPaths;

    initMappings();
  }

  private void initMappings() {
    mappings.withVariable("userId", Lists.newArrayList("me"));
  }

  @Override public CompletableFuture<UrlList> prefixUrls() throws IOException {
    // not supported for partial urls
    throw new UnsupportedOperationException();
  }

  @Override public CompletableFuture<UrlList> siteUrls(HttpUrl url) throws IOException {
    List<CompletableFuture<List<String>>> futures =
        discoveryDocPaths.stream().map(this::singleFuture).collect(toList());

    return join(futures).thenApply(this::flattenList).thenCompose(mappings::replaceVariables);
  }

  private UrlList flattenList(List<List<String>> l) {
    return new UrlList(UrlList.Match.SITE, l.stream().flatMap(List::stream).collect(toList()));
  }

  private CompletableFuture<List<String>> singleFuture(String discoveryDocPath) {
    return discoveryRegistry.load(discoveryDocPath).thenApply(s -> s.getUrls());
  }

  public static GoogleDiscoveryCompleter forApis(DiscoveryRegistry discoveryRegistry,
      List<String> discoveryDocPaths) {
    return new GoogleDiscoveryCompleter(discoveryRegistry, discoveryDocPaths);
  }
}
