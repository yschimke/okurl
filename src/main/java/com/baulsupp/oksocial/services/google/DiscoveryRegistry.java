package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.output.util.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES;

public class DiscoveryRegistry {
  private static final Cache cache =
      new Cache(new File(System.getProperty("user.home"), ".oksocial/google-cache"),
          MEBIBYTES.toBytes(20));

  private static final CacheControl cacheControl =
      new CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build();

  private final OkHttpClient client;
  private final Map<String, Object> map;

  public DiscoveryRegistry(OkHttpClient client, Map<String, Object> map) {
    this.client = client;
    this.map = map;
  }

  // TODO make non synchronous
  public static synchronized DiscoveryRegistry instance(OkHttpClient client) throws IOException {
    client = client.newBuilder().cache(cache).build();

    String url = "https://www.googleapis.com/discovery/v1/apis";
    Request request = new Request.Builder().cacheControl(cacheControl).url(url).build();
    Response response = client.newCall(request).execute();

    try {
      return new DiscoveryRegistry(client, JsonUtil.map(response.body().string()));
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private Map<String, Map<String, Object>> getItems() {
    //noinspection unchecked
    return (Map<String, Map<String, Object>>) map.get("items");
  }

  public CompletableFuture<DiscoveryDocument> load(String discoveryDocPath) {
    Request request =
        new Request.Builder().url(discoveryDocPath).cacheControl(cacheControl).build();
    CompletableFuture<Map<String, Object>> mapFuture =
        AuthUtil.enqueueJsonMapRequest(client, request);

    return mapFuture.thenApply(s -> new DiscoveryDocument(s));
  }
}
