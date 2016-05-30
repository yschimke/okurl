package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SquareUpAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new SquareUpServiceDefinition());

  public static final String NAME = "squareup";

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

    return SquareUpUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising SquareUp API");

    String clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false);
    String clientSecret =
        Secrets.prompt("SquareUp Application Secret", "squareup.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "squareup.scopes", SquareUpUtil.ALL_PERMISSIONS);

    Oauth2Token newCredentials =
        SquareUpAuthFlow.login(client, clientId, clientSecret, scopes);
    credentialsStore.storeCredentials(newCredentials);
  }
}
