package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture;
import com.baulsupp.oksocial.util.ClientException;
import com.baulsupp.oksocial.output.util.FutureUtil;
import com.baulsupp.oksocial.output.util.JsonUtil;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class JsonCredentialsValidator {
  private Request request;
  private Function<Map<String, Object>, String> extractor;
  private Optional<Request> appRequest;
  private Optional<Function<Map<String, Object>, String>> appExtractor;

  public JsonCredentialsValidator(Request request,
      Function<Map<String, Object>, String> extractor) {
    this.request = request;
    this.extractor = extractor;
    this.appRequest = empty();
    this.appExtractor = empty();
  }

  public JsonCredentialsValidator(Request request,
      Function<Map<String, Object>, String> extractor, Request appRequest,
      Function<Map<String, Object>, String> appExtractor) {
    this.request = request;
    this.extractor = extractor;
    this.appRequest = Optional.of(appRequest);
    this.appExtractor = Optional.of(appExtractor);
  }

  public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client) throws IOException {
    CompletableFuture<Optional<String>> nameCallback =
        enqueue(client, request).thenCompose(n -> extractString(n, extractor));

    CompletableFuture<Optional<String>> appCallback = appRequest.map(
        r -> enqueue(client, r).thenCompose(
            response -> extractString(response, appExtractor.get())))
        .orElse(completedFuture(empty()));

    return nameCallback.thenCombine(appCallback,
        (n, c) -> n.map(a -> new ValidatedCredentials(n, c)));
  }

  private CompletableFuture<Response> enqueue(OkHttpClient client, Request r) {
    OkHttpResponseFuture callback = new OkHttpResponseFuture();
    client.newCall(r).enqueue(callback);
    return callback.future;
  }

  private CompletionStage<Optional<String>> extractString(Response response,
      Function<Map<String, Object>, String> responseExtractor) {
    try {
      Map<String, Object> map = JsonUtil.map(response.body().string());

      if (response.code() != 200) {
        String error = "verify failed";
        if (map.containsKey("error")) {
          error += ": " + map.get("error");
        }
        return FutureUtil.failedFuture(new ClientException(
            error, response.code()));
      }

      String name = responseExtractor.apply(map);

      return completedFuture(of(name));
    } catch (IOException e) {
      return FutureUtil.failedFuture(e);
    } finally {
      response.close();
    }
  }

  public static Function<Map<String, Object>, String> fieldExtractor(String name) {
    return map -> (String) map.get(name);
  }
}
