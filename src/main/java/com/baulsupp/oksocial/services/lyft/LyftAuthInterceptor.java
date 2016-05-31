package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import java.io.IOException;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https://developer.lyft.com/docs/authentication
 */
public class LyftAuthInterceptor implements AuthInterceptor<LyftServerCredentials> {
  private final CredentialsStore<LyftServerCredentials> credentialsStore =
      CredentialsStore.create(new LyftServiceDefinition());

  public static final String NAME = "lyft";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Optional<LyftServerCredentials> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
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
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising Lyft API");
    LyftServerCredentials newCredentials = LyftAuthFlow.login(client);
    credentialsStore.storeCredentials(newCredentials);
  }
}
