package com.baulsupp.oksocial.services.spotify;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.services.lyft.LyftUtil;
import com.google.common.collect.Sets;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.Optional.of;

public class SpotifyAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition(host(), "Spotify API", "spotify",
        "https://developer.spotify.com/web-api/endpoint-reference/",
        "https://developer.spotify.com/my-applications/");
  }

  protected String host() {
    return "api.spotify.com";
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    request =
        request.newBuilder().addHeader("Authorization", "Bearer " + token).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Spotify API");

    String clientId =
        Secrets.prompt("Spotify Client Id", "spotify.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Spotify Client Secret", "spotify.clientSecret", "", true);

    Set<String> scopes =
        Secrets.promptArray("Scopes", "spotify.scopes", SpotifyUtil.SCOPES);

    return SpotifyAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    return new BaseUrlCompleter(UrlList.fromResource(name()).get(), hosts());
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        SpotifyUtil.apiRequest("/v1/me", requestBuilder),
        map -> "" + map.get("display_name")).validate(client);
  }

  @Override public Collection<String> hosts() {
    return Collections.unmodifiableSet(Sets.newHashSet(
        "api.spotify.com")
    );
  }

  @Override public boolean canRenew(Oauth2Token credentials) {
    return credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {
    String tokenUrl = "https://accounts.spotify.com/api/token";

    RequestBody body =
        new FormBody.Builder()
            .add("refresh_token", credentials.refreshToken.get())
            .add("grant_type", "refresh_token")
            .build();

    Request request = new Request.Builder().header("Authorization",
        Credentials.basic(credentials.clientId.get(), credentials.clientSecret.get()))
        .url(tokenUrl)
        .method("POST", body)
        .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }
}
