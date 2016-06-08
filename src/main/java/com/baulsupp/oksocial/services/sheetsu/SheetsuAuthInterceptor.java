package com.baulsupp.oksocial.services.sheetsu;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SheetsuAuthInterceptor implements AuthInterceptor<BasicCredentials> {
  private final CredentialsStore<BasicCredentials> credentialsStore =
      CredentialsStore.create(new SheetsuServiceDefinition());
  public static final String NAME = "sheetsu";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Optional<BasicCredentials> credentials = readCredentials();
    if (credentials.isPresent()) {
      BasicCredentials token = readCredentials().get();

      request =
          request.newBuilder()
              .addHeader("Authorization", Credentials.basic(token.user, token.password))
              .build();
    }

    return chain.proceed(request);
  }

  @Override
  public CredentialsStore<BasicCredentials> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return SheetsuUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) {
    String user =
        Secrets.prompt("Sheetsu API Key", "sheetsu.apiKey", "", false);
    String password =
        Secrets.prompt("Sheetsu API Password", "sheetsu.apiSecret", "", true);

    BasicCredentials newCredentials = new BasicCredentials(user, password);

    credentialsStore.storeCredentials(newCredentials);
  }
}
