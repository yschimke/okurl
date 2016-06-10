package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.util.JsonUtil;
import com.baulsupp.oksocial.util.ResponseFutureCallback;
import com.baulsupp.oksocial.util.Util;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class JsonCredentialsValidator {
  private Request request;
  private final Function<Map<String, Object>, String> extractor;

  public JsonCredentialsValidator(Request request,
      Function<Map<String, Object>, String> extractor) {
    this.request = request;
    this.extractor = extractor;
  }

  public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client) throws IOException {
    ResponseFutureCallback callback = new ResponseFutureCallback();
    client.newCall(request).enqueue(callback);

    return callback.future.thenCompose(response -> {
      try {
        Map<String, Object> map = JsonUtil.map(response.body().string());

        if (response.code() != 200) {
          return Util.failedFuture(new IOException(
              "verify failed with " + response.code() + ": " + map.get("error")));
        }

        String name = extractor.apply(map);

        return completedFuture(of(new ValidatedCredentials(name, null)));
      } catch (IOException e) {
        return Util.failedFuture(e);
      } finally {
        response.close();
      }
    });
  }
}
