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
import okhttp3.Response;

public class ServiceInterceptor implements Interceptor {
  private final CredentialsStore credentialsStore;
  private List<AuthInterceptor<?>> services = new ArrayList<>();

  public ServiceInterceptor(CredentialsStore credentialsStore) {
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

    return interceptor.intercept(chain, credentials);
  }

  public Iterable<AuthInterceptor<?>> services() {
    return services;
  }

  public Optional<AuthInterceptor<?>> getByName(String authName) {
    return services.stream().filter(n -> n.name().equals(authName)).findFirst();
  }

  public Optional<AuthInterceptor<?>> getByUrl(String s) {
    HttpUrl url = HttpUrl.parse(s);

    if (url != null) {
      return services.stream().filter(n -> n.supportsUrl(url)).findFirst();
    }

    return Optional.empty();
  }

  public List<String> names() {
    return services.stream().map(AuthInterceptor::name).collect(Collectors.toList());
  }
}
