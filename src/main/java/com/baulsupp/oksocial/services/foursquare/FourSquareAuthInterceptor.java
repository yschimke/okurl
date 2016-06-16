package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FourSquareAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  public static final String NAME = "4sq";

  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.foursquare.com", "FourSquare API");
  }

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<Oauth2Token> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String token = credentials.get().accessToken;

      HttpUrl.Builder urlBuilder = request.url().newBuilder();
      urlBuilder.addQueryParameter("oauth_token", token);
      if (request.url().queryParameter("v") == null) {
        urlBuilder.addQueryParameter("v", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
      }

      request = request.newBuilder().url(urlBuilder.build()).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return FourSquareUtil.API_HOSTS.contains(host);
  }

  @Override
  public Oauth2Token authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising FourSquare API");

    String clientId = Secrets.prompt("FourSquare Application Id", "4sq.clientId", "", false);
    String clientSecret =
        Secrets.prompt("FourSquare Application Secret", "4sq.clientSecret", "", true);

    return FourSquareAuthFlow.login(client, clientId, clientSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        FourSquareUtil.apiRequest("/v2/users/self?v=20160603", requestBuilder),
        map -> getName(map)).validate(client);
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> user =
        (Map<String, Object>) ((Map<String, Object>) map.get("response")).get("user");

    return user.get("firstName") + " " + user.get("lastName");
  }
}
