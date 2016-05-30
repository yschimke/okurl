package com.baulsupp.oksocial.services.stackexchange;

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

public class StackExchangeAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new StackExchangeServiceDefinition());

  public static final String NAME = "stackexchange";

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
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
  }

  @Override
  public CredentialsStore<Oauth2Token> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return StackExchangeUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising StackExchange API");

    String clientId =
        Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false);
    String clientSecret =
        Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "stackexchange.scopes", StackExchangeUtil.SCOPES);

    Oauth2Token newCredentials =
        StackExchangeAuthFlow.login(client, clientId, clientSecret, scopes);
    credentialsStore.storeCredentials(newCredentials);
  }
}
