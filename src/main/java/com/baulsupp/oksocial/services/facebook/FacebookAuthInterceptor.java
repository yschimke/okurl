package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.util.JsonUtil;
import com.baulsupp.oksocial.util.ResponseFutureCallback;
import com.baulsupp.oksocial.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FacebookAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  public static final String NAME = "facebook";

  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new FacebookServiceDefinition());

  public FacebookAuthInterceptor() {
  }

  @Override public String name() {
    return NAME;
  }

  @Override
  public CredentialsStore credentialsStore() {
    return credentialsStore;
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();

    Optional<Oauth2Token> credentials = readCredentials();
    if (credentials.isPresent()) {
      String token = readCredentials().get().accessToken;

      HttpUrl newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build();

      request =
          request.newBuilder().url(newUrl).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return FacebookUtil.API_HOSTS.contains(host);
  }

  @Override
  public void authorize(OkHttpClient client) throws IOException {
    System.err.println("Authorising Facebook API");

    String clientId = Secrets.prompt("Facebook App Id", "facebook.appId", "", false);
    String clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "facebook.scopes",
            Arrays.asList("public_profile", "user_friends", "email"));

    if (scopes.contains("all")) {
      scopes.remove("all");
      scopes.addAll(FacebookUtil.ALL_PERMISSIONS);
    }

    Oauth2Token newCredentials =
        FacebookAuthFlow.login(client, clientId, clientSecret, scopes);
    credentialsStore.storeCredentials(newCredentials);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    Request request =
        FacebookUtil.apiRequest("/me", requestBuilder);
    ResponseFutureCallback callback = new ResponseFutureCallback();
    client.newCall(request).enqueue(callback);

    return callback.future.thenCompose(response -> {
      try {
        Map<String, Object> map = JsonUtil.map(response.body().string());

        if (response.code() != 200) {
          return Util.failedFuture(new IOException(
              "verify failed with " + response.code() + ": " + map.get("error")));
        }

        return CompletableFuture.completedFuture(
            Optional.of(new ValidatedCredentials(String.valueOf(map.get("name")), null)));
      } catch (IOException e) {
        return Util.failedFuture(e);
      } finally {
        response.body().close();
      }
    });
  }
}
