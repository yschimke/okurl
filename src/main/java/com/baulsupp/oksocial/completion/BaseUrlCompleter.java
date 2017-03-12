package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import okhttp3.HttpUrl;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class BaseUrlCompleter extends HostUrlCompleter {
  private final UrlList urlList;
  private CompletionMappings mappings = new CompletionMappings();

  public BaseUrlCompleter(UrlList urlList, Collection<String> hosts) {
    super(hosts);
    this.urlList = urlList;
  }

  @Override public CompletableFuture<UrlList> siteUrls(HttpUrl url) throws IOException {
    CompletableFuture<UrlList> future = completedFuture(urlList);

    return future.thenCompose(r -> mappings.replaceVariables(r));
  }

  public void withVariable(String name, List<String> values) {
    mappings.withVariable(name, values);
  }

  public void withVariable(String name, Supplier<CompletableFuture<List<String>>> values) {
    mappings.withVariable(name, values);
  }
}
