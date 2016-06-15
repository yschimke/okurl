package com.baulsupp.oksocial.services.sheetsu;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SheetsuAuthInterceptor implements AuthInterceptor<BasicCredentials> {
  public static final String NAME = "sheetsu";

  @Override public String name() {
    return NAME;
  }

  @Override public ServiceDefinition<BasicCredentials> serviceDefinition() {
    return new SheetsuServiceDefinition();
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<BasicCredentials> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      BasicCredentials token = credentials.get();

      request =
          request.newBuilder()
              .addHeader("Authorization", Credentials.basic(token.user, token.password))
              .build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return SheetsuUtil.API_HOSTS.contains(host);
  }

  @Override
  public BasicCredentials authorize(OkHttpClient client) {
    String user =
        Secrets.prompt("Sheetsu API Key", "sheetsu.apiKey", "", false);
    String password =
        Secrets.prompt("Sheetsu API Password", "sheetsu.apiSecret", "", true);

    return new BasicCredentials(user, password);
  }
}
