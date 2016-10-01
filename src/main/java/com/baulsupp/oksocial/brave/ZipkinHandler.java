package com.baulsupp.oksocial.brave;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;

public abstract class ZipkinHandler implements Closeable {
  public void configureClient(List<String> commandLineArgs,
      OkHttpClient.Builder clientBuilder) {
  }

  @Override public void close() throws IOException {
  }
}
