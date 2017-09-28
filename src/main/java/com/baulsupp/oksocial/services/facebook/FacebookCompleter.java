package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.completion.HostUrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.baulsupp.oksocial.services.facebook.FacebookUtil.VERSION;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

public class FacebookCompleter extends HostUrlCompleter {
  private static Logger logger = Logger.getLogger(FacebookCompleter.class.getName());

  private OkHttpClient client;

  public FacebookCompleter(OkHttpClient client, Collection<String> hosts) {
    super(hosts);
    this.client = client;
  }

  @Override public CompletableFuture<UrlList> siteUrls(HttpUrl url) throws IOException {
    CompletableFuture<UrlList> result = completePath(url.encodedPath());

    if (!url.encodedPath().endsWith("/")) {
      List<String> parentPaths = url.encodedPathSegments();
      parentPaths.remove(parentPaths.size() - 1);

      String parentPath = "/" + parentPaths.stream().collect(joining("/"));

      result = result.thenCombine(completePath(parentPath), UrlList::combine);
    }

    return result;
  }

  private Function<String, String> addPath(String prefix) {
    return c -> prefix + (prefix.endsWith("/") ? "" : "/") + c;
  }

  private CompletableFuture<List<String>> topLevel() {
    HttpUrl url = HttpUrl.parse(
        "https://graph.facebook.com/" + VERSION + "/me/accounts?fields=username");
    Request request = new Request.Builder().url(url).build();

    return AuthUtil.enqueueJsonMapRequest(client, request)
        .thenApply(m -> concat(
            ((List<Map<String, String>>) m.get("data")).stream().map(v -> v.get("username")),
            of("me")).collect(toList())).exceptionally(ex -> {
              return new ArrayList<>();
            });
  }

  private CompletableFuture<UrlList> completePath(String path) {
    if (path.equals("/")) {
      return topLevel().thenApply(l -> {
        l.add(VERSION);
        return l;
      }).thenApply(l -> new UrlList(UrlList.Match.EXACT,
          l.stream().map(addPath("https://graph.facebook.com/")).collect(toList())));
    } else if (path.matches("/v\\d.\\d/?")) {
      return topLevel().thenApply(
          l -> new UrlList(UrlList.Match.EXACT,
              l.stream().map(addPath("https://graph.facebook.com" + path)).collect(toList())));
    } else {
      String prefix = "https://graph.facebook.com" + path;

      CompletableFuture<FacebookMetadata> metadataFuture =
          FacebookUtil.getMetadata(client, HttpUrl.parse(prefix));

      return metadataFuture.thenApply(
          metadata -> {
            List<String> urls =
                metadata.connections().stream().map(addPath(prefix)).collect(toList());
            urls.add(prefix);
            return new UrlList(UrlList.Match.EXACT, urls);
          })
          .exceptionally(e -> {
            logger.log(Level.FINE, "completion failure", e);
            return new UrlList(UrlList.Match.EXACT, newArrayList());
          });
    }
  }
}
