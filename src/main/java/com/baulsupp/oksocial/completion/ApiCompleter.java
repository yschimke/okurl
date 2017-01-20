package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;

public interface ApiCompleter {
  CompletableFuture<UrlList> prefixUrls() throws IOException;

  CompletableFuture<UrlList> siteUrls(HttpUrl url) throws IOException;
}
