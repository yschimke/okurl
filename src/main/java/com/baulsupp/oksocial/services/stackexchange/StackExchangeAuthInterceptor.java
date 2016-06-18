package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StackExchangeAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.stackexchange.com", "StackExchange API",
        "stackexchange");
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<Oauth2Token> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String token = credentials.get().accessToken;

      request =
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    return StackExchangeUtil.API_HOSTS.contains(url.host());
  }

  @Override
  public Oauth2Token authorize(OkHttpClient client, List<String> authArguments) throws IOException {
    System.err.println("Authorising StackExchange API");

    String clientId =
        Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false);
    String clientSecret =
        Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "stackexchange.scopes", StackExchangeUtil.SCOPES);

    return StackExchangeAuthFlow.login(client, clientId, clientSecret, scopes);
  }
}
