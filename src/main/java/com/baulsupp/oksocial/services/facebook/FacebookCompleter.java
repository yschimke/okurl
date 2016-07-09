package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FacebookCompleter implements ApiCompleter {
  @Override public Future<UrlList> prefixUrls() throws IOException {
    return completedFuture(UrlList.fromResource("facebook").get());
  }

  //public Future<UrlList> matchingUrls(String prefix, OkHttpClient client,
  //    CredentialsStore credentialsStore, CompletionCache completionCache, boolean expensive)
  //    throws IOException {
  //    HttpUrl url = HttpUrl.parse(prefix);
  //
  //    CompletableFuture<FacebookMetadata> metadataFuture = getMetadata(client, url);
  //
  //    CompletableFuture<UrlList> result = metadataFuture.thenApply(
  //        metadata -> new UrlList(
  //            metadata.connections().stream().map(c -> prefix + c).collect(toList())))
  //        .exceptionally(e -> new UrlList(Lists.newArrayList()));
  //
  //    if (!prefix.endsWith("/")) {
  //      CompletableFuture<UrlList> result = metadataFuture.thenApply(
  //          metadata -> new UrlList(
  //              metadata.connections().stream().map(c -> prefix + c).collect(toList())))
  //          .exceptionally(e -> new UrlList(Lists.newArrayList()));
  //
  //
  //      result.thenCombine()
  //    }
  //}

  private CompletableFuture<FacebookMetadata> getMetadata(OkHttpClient client, HttpUrl url)
      throws IOException {
    url = url.newBuilder().addQueryParameter("metadata", "1").build();
    Request request = new Request.Builder().url(url).build();

    return AuthUtil.enqueueJsonMapRequest(client, request)
        .thenApply(m -> new FacebookMetadata((Map<String, Object>) m.get("metadata")));
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.initialise();

    main.urlCompletion = "https://graph.facebook.com/me/";
    main.run();
  }
}
