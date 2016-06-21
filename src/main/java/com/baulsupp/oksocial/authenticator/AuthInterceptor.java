package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public interface AuthInterceptor<T> {
  default String name() {
    return serviceDefinition().shortName();
  }

  boolean supportsUrl(HttpUrl url);

  Response intercept(Interceptor.Chain chain, T credentials) throws IOException;

  T authorize(OkHttpClient client, OutputHandler outputHandler, List<String> authArguments)
      throws IOException;

  ServiceDefinition<T> serviceDefinition();

  default Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, T credentials) throws IOException {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  default boolean canRenew(Response result, T credentials) {
    return false;
  }

  default Optional<T> renew(OkHttpClient client, T credentials) throws IOException {
    return Optional.empty();
  }
}
