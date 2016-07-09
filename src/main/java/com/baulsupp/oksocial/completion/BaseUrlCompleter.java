package com.baulsupp.oksocial.completion;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class BaseUrlCompleter implements ApiCompleter {
  private final String name;
  private final UrlList urlList;
  private List<Function<UrlList, CompletableFuture<UrlList>>> mappings = Lists.newArrayList();

  public BaseUrlCompleter(String name, UrlList urlList) {
    this.name = name;
    this.urlList = urlList;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
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
