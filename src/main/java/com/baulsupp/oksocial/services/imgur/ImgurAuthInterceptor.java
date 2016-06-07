package com.baulsupp.oksocial.services.imgur;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.services.imgur.ImgurUtil.apiRequest;

public class ImgurAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new ImgurServiceDefinition());

  public static final String NAME = "imgur";

  @Override public String name() {
    return NAME;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Optional<Oauth2Token> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().accessToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
  }

  @Override
  public CredentialsStore<Oauth2Token> credentialsStore() {
    return credentialsStore;
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return ImgurUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising Imgur API");

    String clientId =
        Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true);

    Oauth2Token newCredentials = ImgurAuthFlow.login(client, clientId, clientSecret);
    credentialsStore.storeCredentials(newCredentials);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    if (!readCredentials().isPresent()) {
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      return new JsonCredentialsValidator(
          apiRequest("/3/account/me", requestBuilder),
          map -> getName(map)).validate(client);
    }
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> data = (Map<String, Object>) map.get("data");

    return (String) data.get("url");
  }
}
