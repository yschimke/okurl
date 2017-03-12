package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionQuery;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

public class SquareUpAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("connect.squareup.com", "SquareUp API", "squareup",
        "https://docs.connect.squareup.com/api/connect/v2/", "https://connect.squareup.com/apps");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    Request.Builder reqBuilder = request.newBuilder().addHeader("Authorization", "Bearer " + token);
    if (request.header("Accept") == null) {
      reqBuilder.addHeader("Accept", "application/json");
    }
    request = reqBuilder.build();

    return chain.proceed(request);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        SquareUpUtil.apiRequest("/v1/me", requestBuilder), fieldExtractor("name")).validate(
        client);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising SquareUp API");

    String clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false);
    String clientSecret =
        Secrets.prompt("SquareUp Application Secret", "squareup.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "squareup.scopes", SquareUpUtil.ALL_PERMISSIONS);

    return SquareUpAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    Optional<UrlList> urlList =
        UrlList.fromResource(name());

    Optional<Oauth2Token> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());

    BaseUrlCompleter completer = new BaseUrlCompleter(urlList.get(), hosts());

    credentials.ifPresent(oauth2Token -> completer.withVariable("location",
        () -> completionVariableCache.compute(name(), "locations",
            () -> CompletionQuery.getIds(client, "https://connect.squareup.com/v2/locations",
                "locations",
                "id"))));

    return completer;
  }

  @Override public Collection<String> hosts() {
    return SquareUpUtil.API_HOSTS;
  }
}
