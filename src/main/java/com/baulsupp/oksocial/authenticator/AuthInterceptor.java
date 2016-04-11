package com.baulsupp.oksocial.authenticator;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public interface AuthInterceptor extends Interceptor {
  String mapUrl(String alias, String url);

  boolean supportsUrl(HttpUrl url);

  void authorize(OkHttpClient client);
}
