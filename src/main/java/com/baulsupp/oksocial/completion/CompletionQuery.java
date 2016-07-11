package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.stream.Collectors.toList;

public class CompletionQuery {
  public static CompletableFuture<List<String>> getIds(OkHttpClient client, String urlString,
      String path,
      String key) {
    Request request = new Request.Builder().url(urlString).build();

    return AuthUtil.enqueueJsonMapRequest(client, request)
        .thenApply(map -> {
          List<Map<String, Object>> surveys = (List<Map<String, Object>>) map.get(path);
          return surveys.stream().map(m -> (String) m.get(key)).collect(toList());
        });
  }
}
