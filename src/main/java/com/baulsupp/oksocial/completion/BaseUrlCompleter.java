package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class BaseUrlCompleter implements ApiCompleter {
  private final String name;
  private final UrlList urlList;

  public BaseUrlCompleter(String name, UrlList urlList) {
    this.name = name;
    this.urlList = urlList;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    return completedFuture(urlList);
  }

  public void withVariable(String name, List<String> values, boolean keep) {
    //urls = urls.replace(name, Lists.newArrayList(credentials.get().user), false);
  }

  public void withVariable(String name, Supplier<Future<List<String>>> values, boolean keep) {
    //completionCache.store(serviceDefinition().shortName(), "surveys", surveys);

  }
}
