package com.baulsupp.oksocial.facebook;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FacebookAuthInterceptor implements AuthInterceptor<FacebookCredentials> {
  public static final String NAME = "facebook";

  private final CredentialsStore<FacebookCredentials> credentialsStore =
      CredentialsStore.create(new FacebookServiceDefinition());

  private FacebookCredentials credentials = null;

  public FacebookAuthInterceptor() {
  }

  @Override public String name() {
    return NAME;
  }

  public FacebookCredentials credentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override
  public CredentialsStore credentialsStore() {
    return credentialsStore;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    if (credentials() != null) {
      String token = credentials().accessToken;

      HttpUrl newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build();

      request =
          request.newBuilder().url(newUrl).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return FacebookUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    System.err.println("Authorising Facebook API");

    String clientId = Secrets.prompt("Facebook App Id", "facebook.appId", false);
    String clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", true);
    Set<String> scopes = new HashSet<>(
        Arrays.asList(Secrets.prompt("Scopes", "facebook.scopes", false).split(",")));

    FacebookCredentials newCredentials =
        LoginAuthFlow.login(client, clientId, clientSecret, scopes);
    CredentialsStore<FacebookCredentials> facebookCredentialsStore =
        new OSXCredentialsStore<>(new FacebookServiceDefinition());
    facebookCredentialsStore.storeCredentials(newCredentials);
  }
}
