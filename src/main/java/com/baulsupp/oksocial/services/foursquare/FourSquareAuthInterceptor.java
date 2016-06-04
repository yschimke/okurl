package com.baulsupp.oksocial.services.foursquare;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.util.JsonUtil;
import com.baulsupp.oksocial.util.ResponseFutureCallback;
import com.baulsupp.oksocial.util.Util;
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
    }

    Request request =
        FourSquareUtil.apiRequest("/v2/users/self?v=20160603", requestBuilder);
    ResponseFutureCallback callback = new ResponseFutureCallback();
    client.newCall(request).enqueue(callback);

    return callback.future.thenCompose(response -> {
      try {
        Map<String, Object> map = JsonUtil.map(response.body().string());

        if (response.code() != 200) {
          return Util.failedFuture(new IOException(
              "verify failed with " + response.code() + ": " + map.get("error")));
        }

        Map<String, Object> user =
            (Map<String, Object>) ((Map<String, Object>) map.get("response")).get("user");

        String name = user.get("firstName") + " " + user.get("lastName");

        return CompletableFuture.completedFuture(Optional.of(new ValidatedCredentials(name, null)));
      } catch (IOException e) {
        return Util.failedFuture(e);
      } finally {
        response.body().close();
      }
    });
  }
}
