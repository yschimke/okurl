package com.baulsupp.oksocial.services.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UberAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new Oauth2ServiceDefinition("api.uber.com", "Uber API"));
  public static final String NAME = "uber";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    Optional<Oauth2Token> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().accessToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
  }

  @Override
  public CredentialsStore<Oauth2Token> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return UberUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising Uber API");

    String clientId =
        Secrets.prompt("Uber Client Id", "uber.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Uber Client Secret", "uber.clientSecret", "", true);

    Oauth2Token newCredentials = UberAuthFlow.login(client, clientId, clientSecret);

    credentialsStore.storeCredentials(newCredentials);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    if (!readCredentials().isPresent()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return new JsonCredentialsValidator(
          UberUtil.apiRequest("/v1/me", requestBuilder),
          map -> map.get("first_name") + " " + map.get("last_name")).validate(client);
    }
  }
}
