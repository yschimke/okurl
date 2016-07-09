package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.util.ClientException;
import com.baulsupp.oksocial.util.JsonUtil;
import java.io.IOException;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthUtil {
  public static String makeSimpleRequest(OkHttpClient client, Request request) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        if (response.code() >= 400 && response.code() < 500) {
          throw new ClientException(response.message(), response.code());
        }

        String message = response.body().string();

        if (message.length() == 0) {
          message = response.message();
        }

        throw new IllegalStateException(
            "failed request " + response.code() + ": " + message);
      }

      return response.body().string();
    }
  }

  public static String makeSimpleGetRequest(OkHttpClient client, String uri) throws IOException {
    return makeSimpleRequest(client, uriGetRequest(uri));
  }

  public static Request uriGetRequest(String uri) {
    return new Request.Builder().url(uri).build();
  }

  public static Map<String, Object> makeJsonMapRequest(OkHttpClient client, Request request)
      throws IOException {
    return JsonUtil.map(makeSimpleRequest(client, request));
  }
}
