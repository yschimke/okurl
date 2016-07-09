package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FacebookCompleter implements ApiCompleter {
  private OkHttpClient client;

  public FacebookCompleter(OkHttpClient client) {
    this.client = client;
  }

  @Override public Future<UrlList> prefixUrls() throws IOException {
    return completedFuture(
        UrlList.fromResource(quote("https://graph.facebook.com/") + ".*", "facebook").get());
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
    CompletableFuture<UrlList> result = completePath(url.encodedPath());

    if (!url.encodedPath().endsWith("/")) {
      List<String> parentPaths = url.encodedPathSegments();
      parentPaths.remove(parentPaths.size() - 1);

      String parentPath = "/" + parentPaths.stream().collect(joining("/"));

      result = result.thenCombine(completePath(parentPath), (a, b) -> a.combine(b));
    }

    return result;
  }

  private CompletableFuture<UrlList> completePath(String path) {
    String prefix = "https://graph.facebook.com" + path;

    CompletableFuture<FacebookMetadata> metadataFuture = getMetadata(client, HttpUrl.parse(prefix));

    return metadataFuture.thenApply(
        metadata -> new UrlList(quote(prefix) + ".*",
            metadata.connections().stream().map(c -> prefix + "/" + c).collect(toList())))
        .exceptionally(e -> new UrlList(quote(prefix) + ".*", newArrayList()));
  }

  private CompletableFuture<FacebookMetadata> getMetadata(OkHttpClient client, HttpUrl url) {
    url = url.newBuilder().addQueryParameter("metadata", "1").build();
    Request request = new Request.Builder().url(url).build();

    return AuthUtil.enqueueJsonMapRequest(client, request)
        .thenApply(m -> new FacebookMetadata((Map<String, Object>) m.get("metadata")));
  }
}
