package com.baulsupp.oksocial.stackoverflow;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.facebook.*;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.api.client.util.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StackOverflowAuthInterceptor implements AuthInterceptor<StackOverflowCredentials> {
  private final CredentialsStore<StackOverflowCredentials> credentialsStore =
      CredentialsStore.create(new StackOverflowServiceDefinition());
  private StackOverflowCredentials credentials = null;

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

  public StackOverflowCredentials credentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override
  public CredentialsStore<StackOverflowCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return StackOverflowUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    System.err.println("Authorising StackExchange API");

    String clientId = Secrets.prompt("Facebook Client Id", "stackexchange.clientId", false);
    String clientSecret = Secrets.prompt("Facebook Client Secret", "stackexchange.clientSecret", true);
    Set<String> scopes = new HashSet<>(
        Arrays.asList(Secrets.prompt("Scopes", "stackexchange.scopes", false).split(",")));

    FacebookCredentials newCredentials =
        com.baulsupp.oksocial.facebook.LoginAuthFlow.login(client, clientId, clientSecret, scopes);
    CredentialsStore<FacebookCredentials> facebookCredentialsStore =
        new OSXCredentialsStore<>(new FacebookServiceDefinition());
    facebookCredentialsStore.storeCredentials(newCredentials);
  }
}
