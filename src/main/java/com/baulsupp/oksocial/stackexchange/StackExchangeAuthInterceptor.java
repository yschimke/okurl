package com.baulsupp.oksocial.stackexchange;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StackExchangeAuthInterceptor implements AuthInterceptor<StackExchangeCredentials> {
  private final CredentialsStore<StackExchangeCredentials> credentialsStore =
      CredentialsStore.create(new StackExchangeServiceDefinition());
  private StackExchangeCredentials credentials = null;

  public static final String NAME = "stackexchange";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (credentials() != null) {
      String token = credentials().serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
  }

  public StackExchangeCredentials credentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override
  public CredentialsStore<StackExchangeCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return StackExchangeUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    System.err.println("Authorising StackExchange API");

    String clientId = Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", false);
    String clientSecret = Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret", true);
    Set<String> scopes = new HashSet<>(
        Arrays.asList(Secrets.prompt("Scopes", "stackexchange.scopes", false).split(",")));

    StackExchangeCredentials newCredentials =
        LoginAuthFlow.login(client, clientId, clientSecret, scopes);
    CredentialsStore<StackExchangeCredentials> stackOverflowCredentialsStore =
        new OSXCredentialsStore<>(new StackExchangeServiceDefinition());
    stackOverflowCredentialsStore.storeCredentials(newCredentials);
  }
}
