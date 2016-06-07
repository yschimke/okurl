package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https://developer.google.com/docs/authentication
 */
public class GoogleAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new GoogleServiceDefinition());

  public static final String NAME = "google";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
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

    return GoogleUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising Google API");

    String clientId =
        Secrets.prompt("Google Client Id", "google.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Google Client Secret", "google.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "google.scopes", GoogleUtil.SCOPES);

    Oauth2Token newCredentials = GoogleAuthFlow.login(client, clientId, clientSecret, scopes);
    credentialsStore.storeCredentials(newCredentials);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    if (!readCredentials().isPresent()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return new JsonCredentialsValidator(
          requestBuilder.url("https://www.googleapis.com/oauth2/v3/userinfo").build(),
          map -> (String) map.get("name")).validate(client);
    }
  }
}
