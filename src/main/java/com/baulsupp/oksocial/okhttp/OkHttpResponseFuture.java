package com.baulsupp.oksocial.okhttp;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OkHttpResponseFuture implements Callback {
  public final CompletableFuture<Response> future = new CompletableFuture<>();
  public Call call;

  public OkHttpResponseFuture(Call call) {
    this.call = call;
  }

  @Override public void onFailure(Call call, IOException e) {
    future.completeExceptionally(e);
  }

  @Override public void onResponse(Call call, Response response) throws IOException {
    future.complete(response);
  }
}
