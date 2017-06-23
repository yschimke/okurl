package com.baulsupp.oksocial.okhttp;

import com.baulsupp.oksocial.output.ResponseExtractor;
import com.baulsupp.oksocial.output.util.JsonUtil;
import java.util.List;
import java.util.Optional;
import okhttp3.Response;
import okio.BufferedSource;

import static java.util.Optional.of;

public class OkHttpResponseExtractor implements ResponseExtractor<Response> {
  @Override public Optional<String> mimeType(Response response) {
    if (response.body() == null || response.body().contentType() == null) {
      return Optional.empty();
    }

    if (response.request().url().host().equals("graph.facebook.com") &&
        response.body().contentType().toString().equals("text/javascript")) {
      return of(JsonUtil.JSON);
    }

    return of(response.body().contentType().toString());
  }

  @Override public BufferedSource source(Response response) {
    return response.body().source();
  }

  @Override public String filename(Response response) {
    List<String> segments = response.request().url().pathSegments();
    String name = segments.get(segments.size() - 1);

    return name;
  }
}
