package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public interface AuthInterceptor<T> extends Interceptor {
  String name();

  boolean supportsUrl(HttpUrl url);

  void authorize(OkHttpClient client);

  CredentialsStore<T> credentialsStore();

  T credentials();
}
