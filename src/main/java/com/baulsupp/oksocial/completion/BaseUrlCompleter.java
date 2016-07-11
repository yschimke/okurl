package com.baulsupp.oksocial.completion;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import okhttp3.HttpUrl;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class BaseUrlCompleter extends HostUrlCompleter {
  private final UrlList urlList;
  private List<Function<UrlList, CompletableFuture<UrlList>>> mappings = Lists.newArrayList();

  public BaseUrlCompleter(UrlList urlList, Collection<String> hosts) {
    super(hosts);
    this.urlList = urlList;
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    CompletableFuture<UrlList> future = completedFuture(urlList);

    for (Function<UrlList, CompletableFuture<UrlList>> s : mappings) {
      future = future.thenCompose(s);
    }

    return future;
  }

  public void withVariable(String name, List<String> values) {
    withVariable(name, () -> completedFuture(values));
  }

  public void withVariable(String name, Supplier<CompletableFuture<List<String>>> values) {
    mappings.add(ul -> values.get().thenApply(l -> ul.replace(name, l, true)));
  }
}
