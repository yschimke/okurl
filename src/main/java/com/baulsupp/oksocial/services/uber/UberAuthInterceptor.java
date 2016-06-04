package com.baulsupp.oksocial.services.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UberAuthInterceptor implements AuthInterceptor<UberServerCredentials> {
  private final CredentialsStore<UberServerCredentials> credentialsStore =
      CredentialsStore.create(new UberServiceDefinition());
  public static final String NAME = "uber";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    Optional<UberServerCredentials> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
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
    String serverToken =
        Secrets.prompt("Uber Server Token", "uber.serverToken", "", true);

    UberServerCredentials newCredentials = new UberServerCredentials(serverToken);

    credentialsStore.storeCredentials(newCredentials);
  }
}
