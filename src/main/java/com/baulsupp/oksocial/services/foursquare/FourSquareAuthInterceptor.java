package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.secrets.Secrets;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FourSquareAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.foursquare.com", "FourSquare API", "4sq",
        "https://developer.foursquare.com/docs/", "https://foursquare.com/developers/apps");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    HttpUrl.Builder urlBuilder = request.url().newBuilder();
    urlBuilder.addQueryParameter("oauth_token", token);
    if (request.url().queryParameter("v") == null) {
      urlBuilder.addQueryParameter("v", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    request = request.newBuilder().url(urlBuilder.build()).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising FourSquare API");

    String clientId = Secrets.prompt("FourSquare Application Id", "4sq.clientId", "", false);
    String clientSecret =
        Secrets.prompt("FourSquare Application Secret", "4sq.clientSecret", "", true);

    return FourSquareAuthFlow.login(client, outputHandler, clientId, clientSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        FourSquareUtil.apiRequest("/v2/users/self?v=20160603", requestBuilder),
        this::getName).validate(client);
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> user =
        (Map<String, Object>) ((Map<String, Object>) map.get("response")).get("user");

    return user.get("firstName") + " " + user.get("lastName");
  }

  @Override public Collection<String> hosts() {
    return FourSquareUtil.API_HOSTS;
  }
}
