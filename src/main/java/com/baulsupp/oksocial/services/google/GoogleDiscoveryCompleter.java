package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.stream.Collectors.toList;

public class GoogleDiscoveryCompleter implements ApiCompleter {
  private OkHttpClient client;
  private final List<String> discoveryDocPaths;

  public GoogleDiscoveryCompleter(OkHttpClient client, List<String> discoveryDocPaths) {
    this.client = client;
    this.discoveryDocPaths = discoveryDocPaths;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    // not supported for partial urls
    throw new UnsupportedOperationException();
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    List<CompletableFuture<List<String>>> futures =
        discoveryDocPaths.stream().map(this::singleFuture).collect(toList());

    return all(futures).thenApply(this::flattenList);
  }

  private UrlList flattenList(List<List<String>> l) {
    return new UrlList(UrlList.Match.SITE, l.stream().flatMap(List::stream).collect(toList()));
  }

  public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures) {
    CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);

    return CompletableFuture.allOf(cfs)
        .thenApply(v -> futures.stream().
            map(CompletableFuture::join).
            collect(Collectors.toList())
        );
  }

  private CompletableFuture<List<String>> singleFuture(String discoveryDocPath) {
    Request request = new Request.Builder().url(discoveryDocPath).build();
    CompletableFuture<Map<String, Object>> mapFuture =
        AuthUtil.enqueueJsonMapRequest(client, request);

    return mapFuture.thenApply(this::buildUrlList);
  }

  private List<String> buildUrlList(Map<String, Object> map) {
    DiscoveryDocument doc = new DiscoveryDocument(map);

    return doc.getUrls();
  }

  public static GoogleDiscoveryCompleter forApis(OkHttpClient client,
      List<String> discoveryDocPaths) {
    return new GoogleDiscoveryCompleter(client, discoveryDocPaths);
  }
}
