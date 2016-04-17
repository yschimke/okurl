package com.baulsupp.oksocial.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.google.common.collect.Sets;
import okhttp3.*;

import java.io.IOException;
import java.util.Set;

public class UberAuthInterceptor implements AuthInterceptor<UberServerCredentials> {
  private final CredentialsStore<UberServerCredentials> credentialsStore = CredentialsStore.create(new UberServiceDefinition());
  private UberServerCredentials credentials = null;

  public static final String NAME = "uber";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
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

  @Override
  public CredentialsStore<UberServerCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return UberUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    char[] password = System.console().readPassword("Uber Server Token: ");

    if (password != null) {
      UberServerCredentials newCredentials = new UberServerCredentials(new String(password));

      CredentialsStore<UberServerCredentials> uberCredentialsStore =
          new OSXCredentialsStore<>(new UberServiceDefinition());

      uberCredentialsStore.storeCredentials(newCredentials);
    }
  }
}
