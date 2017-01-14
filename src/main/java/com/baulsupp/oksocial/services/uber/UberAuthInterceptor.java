package com.baulsupp.oksocial.services.uber;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.Optional.of;

public class UberAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition(host(), "Uber API", "uber",
        "https://developer.uber.com/docs/riders/references/api");
  }

  protected String host() {
    return "api.uber.com";
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
    System.err.println("Authorising Uber API");

    String clientId =
        Secrets.prompt("Uber Client Id", "uber.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Uber Client Secret", "uber.clientSecret", "", true);

    return UberAuthFlow.login(client, outputHandler, clientId, clientSecret);
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    return new BaseUrlCompleter(UrlList.fromResource(name()).get(), hosts());
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        UberUtil.apiRequest("/v1/me", requestBuilder),
        map -> map.get("first_name") + " " + map.get("last_name")).validate(client);
  }

  @Override public Collection<String> hosts() {
    return Collections.unmodifiableSet(Sets.newHashSet(
        "api.uber.com", "login.uber.com", "sandbox-api.uber.com")
    );
  }

  @Override public boolean canRenew(Response result, Oauth2Token credentials) {
    return result.code() == 401
        && credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {
    String tokenUrl = "https://login.uber.com/oauth/v2/token";

    RequestBody body =
        new FormBody.Builder().add("client_id", credentials.clientId.get())
            //.add("redirect_uri", s.getRedirectUri())
            .add("client_secret", credentials.clientSecret.get())
            .add("refresh_token", credentials.refreshToken.get())
            .add("grant_type", "refresh_token")
            .build();

    Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }
}
