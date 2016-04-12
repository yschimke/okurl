package com.baulsupp.oksocial.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UberAuthInterceptor implements AuthInterceptor<UberServerCredentials> {
  private final CredentialsStore<UberServerCredentials> credentialsStore = CredentialsStore.create(new UberServiceDefinition());
  private UberServerCredentials credentials = null;

  @Override
  public Set<String> aliasNames() {
    return Sets.newHashSet("uberapi");
  }

  @Override public String mapUrl(String alias, String url) {
    switch (alias) {
      case "uberapi":
        return "https://api.uber.com" + url;
      default:
        return null;
    }
  }

  @Override public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    if (credentials() != null) {
      String token = credentials().serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
  }

  public UberServerCredentials credentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override public CredentialsStore<UberServerCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return UberUtil.API_HOSTS.contains(host);
  }

  @Override public void authorize(OkHttpClient client) {
    char[] password = System.console().readPassword("Uber Server Token: ");

    if (password != null) {
      UberServerCredentials newCredentials = new UberServerCredentials(new String(password));

      CredentialsStore<UberServerCredentials> uberCredentialsStore =
          new OSXCredentialsStore<>(new UberServiceDefinition());

      uberCredentialsStore.storeCredentials(newCredentials);
    }
  }
}
