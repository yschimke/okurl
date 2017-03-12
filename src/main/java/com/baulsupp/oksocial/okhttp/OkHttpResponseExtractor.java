package com.baulsupp.oksocial.okhttp;

import ee.schimke.oksocial.output.ResponseExtractor;
import java.util.Optional;
import okhttp3.Response;
import okio.BufferedSource;

public class OkHttpResponseExtractor implements ResponseExtractor<Response> {
  @Override public Optional<String> mimeType(Response response) {
    return null;
  }

  @Override public BufferedSource source(Response response) {
    return null;
  }

  @Override public String filename(Response response) {
    return null;
  }
}
