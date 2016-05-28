package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public interface AuthInterceptor<T> extends Interceptor {
  String name();

  boolean supportsUrl(HttpUrl url);

  void authorize(OkHttpClient client);

  CredentialsStore<T> credentialsStore();

  T credentials();

  default Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  ;
}
