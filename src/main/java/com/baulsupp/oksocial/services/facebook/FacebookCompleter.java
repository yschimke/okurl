package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.completion.HostUrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FacebookCompleter extends HostUrlCompleter {
  private static Logger logger = Logger.getLogger(FacebookCompleter.class.getName());

  private OkHttpClient client;

  public FacebookCompleter(OkHttpClient client, Collection<String> hosts) {
    super(hosts);
    this.client = client;
  }

  @Override public Future<UrlList> siteUrls(HttpUrl url) throws IOException {
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
    // TODO add accounts, albums, groups
    return completedFuture(Lists.newArrayList("me"));
  }

  private CompletableFuture<UrlList> completePath(String path) {
    if (path.equals("/")) {
      return topLevel().thenApply(l -> {
        l.add("v2.8");
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
          getMetadata(client, HttpUrl.parse(prefix));

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

  private CompletableFuture<FacebookMetadata> getMetadata(OkHttpClient client, HttpUrl url) {
    url = url.newBuilder().addQueryParameter("metadata", "1").build();
    Request request = new Request.Builder().url(url).build();

    return AuthUtil.enqueueJsonMapRequest(client, request)
        .thenApply(m -> new FacebookMetadata((Map<String, Object>) m.get("metadata")));
  }
}
