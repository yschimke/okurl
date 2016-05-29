package com.baulsupp.oksocial.authenticator;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthUtil {
  public static String makeSimpleRequest(OkHttpClient client, Request request) throws IOException {
    Response response = client.newCall(request).execute();

    try {
      if (!response.isSuccessful()) {
        throw new IllegalStateException(
            "failed request " + response.code() + ": " + response.message());
      }

      return response.body().string();
    } finally {
      response.body().close();
    }
  }

  public static String makeSimpleGetRequest(OkHttpClient client, String uri) throws IOException {
    return makeSimpleRequest(client, uriGetRequest(uri));
  }

  public static Request uriGetRequest(String uri) {
    return new Request.Builder().url(uri).build();
  }
}
