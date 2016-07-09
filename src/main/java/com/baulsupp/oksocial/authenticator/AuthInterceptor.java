package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionCache;
import com.baulsupp.oksocial.completion.HostUrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;

public interface AuthInterceptor<T> {
  default String name() {
    return serviceDefinition().shortName();
  }

  default boolean supportsUrl(HttpUrl url) {
    return hosts().contains(url.host());
  }

  Response intercept(Interceptor.Chain chain, T credentials) throws IOException;

  T authorize(OkHttpClient client, OutputHandler outputHandler, List<String> authArguments)
      throws IOException;

  ServiceDefinition<T> serviceDefinition();

  default Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, T credentials) throws IOException {
    return completedFuture(empty());
  }

  default boolean canRenew(Response result, T credentials) {
    return false;
  }

  default Optional<T> renew(OkHttpClient client, T credentials) throws IOException {
    return empty();
  }

  Collection<String> hosts();

  default ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionCache completionCache) throws IOException {
    Optional<UrlList> urlList = UrlList.fromResource(name());

    if (urlList.isPresent()) {
      return new BaseUrlCompleter(name(), urlList.get());
    } else {
      return new HostUrlCompleter(hosts());
    }
  }
}
