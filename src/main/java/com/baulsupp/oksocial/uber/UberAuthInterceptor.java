package com.baulsupp.oksocial.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UberAuthInterceptor implements AuthInterceptor {
  private final CredentialsStore<UberServerCredentials> credentialsStore;
  private UberServerCredentials credentials = null;

  public UberAuthInterceptor() {
    credentialsStore = CredentialsStore.create(new UberOSXCredentials());
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

    String token = getCredentials().serverToken;

    request =
        request.newBuilder().addHeader("Authorization", "Token " + token).build();

    return chain.proceed(request);
  }

  private UberServerCredentials getCredentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return getCredentials() != null && UberUtil.API_HOSTS.contains(host);
  }

  @Override public void authorize(OkHttpClient client) {
    char[] password = System.console().readPassword("Uber Server Token: ");

    if (password != null) {
      UberServerCredentials newCredentials = new UberServerCredentials(new String(password));

      CredentialsStore<UberServerCredentials> uberCredentialsStore =
          new OSXCredentialsStore<>(new UberOSXCredentials());

      uberCredentialsStore.storeCredentials(newCredentials);
    }
  }
}
