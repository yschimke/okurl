package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.Set;

public interface AuthInterceptor<T> extends Interceptor {
  String mapUrl(String alias, String url);

  Set<String> aliasNames();

  boolean supportsUrl(HttpUrl url);

  void authorize(OkHttpClient client);

  CredentialsStore<T> credentialsStore();

  T credentials();
}
