package com.baulsupp.oksocial.lyft;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https://developer.lyft.com/docs/authentication
 */
public class LyftAuthInterceptor implements AuthInterceptor<LyftServerCredentials> {
  private final CredentialsStore<LyftServerCredentials> credentialsStore = CredentialsStore.create(new LyftServiceDefinition());
  private LyftServerCredentials credentials = null;

  public static final String NAME = "lyft";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (credentials() != null) {
      String token = credentials().serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
  }

  public LyftServerCredentials credentials() {
    if (credentials == null) {
      credentials = credentialsStore.readDefaultCredentials();
    }

    return credentials;
  }

  @Override
  public CredentialsStore<LyftServerCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return LyftUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    try {
      System.err.println("Authorising Lyft API");
      LyftServerCredentials newCredentials = LyftAuthFlow.login(client);
      CredentialsStore<LyftServerCredentials> lyftCredentialsStore =
          new OSXCredentialsStore<>(new LyftServiceDefinition());
      lyftCredentialsStore.storeCredentials(newCredentials);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
