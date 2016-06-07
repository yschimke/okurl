package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FourSquareAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new FourSquareServiceDefinition());

  public static final String NAME = "4sq";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Optional<Oauth2Token> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().accessToken;

      HttpUrl.Builder urlBuilder = request.url().newBuilder();
      urlBuilder.addQueryParameter("oauth_token", token);
      if (request.url().queryParameter("v") == null) {
        urlBuilder.addQueryParameter("v", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
      }

      request = request.newBuilder().url(urlBuilder.build()).build();
    }

    return chain.proceed(request);
  }

  @Override
  public CredentialsStore<Oauth2Token> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return FourSquareUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising FourSquare API");

    String clientId = Secrets.prompt("FourSquare Application Id", "4sq.clientId", "", false);
    String clientSecret =
        Secrets.prompt("FourSquare Application Secret", "4sq.clientSecret", "", true);

    Oauth2Token newCredentials =
        FourSquareAuthFlow.login(client, clientId, clientSecret);
    credentialsStore.storeCredentials(newCredentials);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    if (!readCredentials().isPresent()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return new JsonCredentialsValidator(
          FourSquareUtil.apiRequest("/v2/users/self?v=20160603", requestBuilder),
          map -> getName(map)).validate(client);
    }
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> user =
        (Map<String, Object>) ((Map<String, Object>) map.get("response")).get("user");

    return user.get("firstName") + " " + user.get("lastName");
  }
}
