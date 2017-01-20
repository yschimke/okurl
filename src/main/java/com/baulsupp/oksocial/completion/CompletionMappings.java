package com.baulsupp.oksocial.completion;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class CompletionMappings {
  private List<Function<UrlList, CompletableFuture<UrlList>>> mappings = Lists.newArrayList();

  public void withVariable(String name, List<String> values) {
    withVariable(name, () -> completedFuture(values));
  }

  public void withVariable(String name, Supplier<CompletableFuture<List<String>>> values) {
    mappings.add(ul -> values.get().thenApply(l -> ul.replace(name, l, true)));
  }

  public CompletableFuture<UrlList> replaceVariables(UrlList urlList) {
    CompletableFuture<UrlList> future = CompletableFuture.completedFuture(urlList);

    for (Function<UrlList, CompletableFuture<UrlList>> s : mappings) {
      future = future.thenCompose(s);
    }

    return future;
  }
}
