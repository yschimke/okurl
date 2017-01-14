package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ServiceInterceptor implements Interceptor {
  private final CredentialsStore credentialsStore;
  private List<AuthInterceptor<?>> services = new ArrayList<>();
  private OkHttpClient authClient = null;

  public ServiceInterceptor(OkHttpClient authClient, CredentialsStore credentialsStore) {
    this.authClient = authClient;
    this.credentialsStore = credentialsStore;
    ServiceLoader.load(AuthInterceptor.class, AuthInterceptor.class.getClassLoader())
        .iterator().forEachRemaining(services::add);
  }

  @Override public Response intercept(Chain chain) throws IOException {
    for (AuthInterceptor interceptor : services) {
      if (interceptor.supportsUrl(chain.request().url())) {
        return intercept(interceptor, chain);
      }
    }

    return chain.proceed(chain.request());
  }

  private <T> Response intercept(AuthInterceptor<T> interceptor, Chain chain) throws IOException {
    Optional<T> credentials =
        credentialsStore.readDefaultCredentials(interceptor.serviceDefinition());

    if (!credentials.isPresent()) {
      credentials = interceptor.defaultCredentials();
    }

    if (credentials.isPresent()) {
      Response result = interceptor.intercept(chain, credentials.get());

      if (result.code() >= 400 && result.code() < 500) {
        if (interceptor.canRenew(result) && interceptor.canRenew(credentials.get())) {
          Optional<T> newCredentials = interceptor.renew(authClient, credentials.get());

          if (newCredentials.isPresent()) {
            credentialsStore.storeCredentials(newCredentials.get(),
                interceptor.serviceDefinition());
          }
        }
      }

      return result;
    } else {
      return chain.proceed(chain.request());
    }
  }

  public List<AuthInterceptor<?>> services() {
    return services;
  }

  public Optional<AuthInterceptor<?>> getByName(String authName) {
    return services.stream().filter(n -> n.name().equals(authName)).findFirst();
  }

  public Optional<AuthInterceptor<?>> getByUrl(String url) {
    HttpUrl httpUrl = HttpUrl.parse(url);

    if (httpUrl != null) {
      return services.stream().filter(n -> n.supportsUrl(httpUrl)).findFirst();
    }

    return Optional.empty();
  }

  public Optional<AuthInterceptor<?>> findAuthInterceptor(String nameOrUrl) {
    Optional<AuthInterceptor<?>> auth = getByName(nameOrUrl);

    if (!auth.isPresent()) {
      auth = getByUrl(nameOrUrl);
    }

    return auth;
  }

  public List<String> names() {
    return services.stream().map(AuthInterceptor::name).collect(Collectors.toList());
  }
}
