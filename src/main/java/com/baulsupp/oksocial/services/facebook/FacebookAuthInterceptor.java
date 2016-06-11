package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.services.facebook.FacebookUtil.apiRequest;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class FacebookAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  public static final String NAME = "facebook";

  private final CredentialsStore<Oauth2Token> credentialsStore =
      CredentialsStore.create(new Oauth2ServiceDefinition("graph.facebook.com", "Facebook API"));

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

  private String extract(Map<String, Object> map) {
    return "" + map.get("name") + " (" + map.get("id") + ")";
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder) throws IOException {
    if (!readCredentials().isPresent()) {
      return completedFuture(empty());
    } else {
      return new JsonCredentialsValidator(apiRequest("/me", requestBuilder), this::extract,
          apiRequest("/app", requestBuilder), this::extract).validate(client);
    }
  }
}
