package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
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

public class SquareUpAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("connect.squareup.com", "SquareUp API", "squareup");
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<Oauth2Token> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String token = credentials.get().accessToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    return SquareUpUtil.API_HOSTS.contains(url.host());
  }

  @Override
  public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising SquareUp API");

    String clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false);
    String clientSecret =
        Secrets.prompt("SquareUp Application Secret", "squareup.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "squareup.scopes", SquareUpUtil.ALL_PERMISSIONS);

    return SquareUpAuthFlow.login(client, clientId, clientSecret, scopes);
  }
}
