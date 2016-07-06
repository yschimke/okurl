package com.baulsupp.oksocial.services.squareup;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.CompletionCache;
import com.baulsupp.oksocial.completion.CompletionQuery;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;
import static java.util.stream.Collectors.toList;

public class SquareUpAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("connect.squareup.com", "SquareUp API", "squareup");
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

  @Override public Future<List<String>> matchingUrls(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionCache completionCache, boolean expensive)
      throws IOException {
    UrlList urlList = UrlList.fromResource("squareup").get();

    Optional<Oauth2Token> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());

    if (credentials.isPresent()) {
      Optional<List<String>> surveysOpt =
          completionCache.get(serviceDefinition().shortName(), "locations", expensive);

      List<String> locations;
      if (!surveysOpt.isPresent()) {
        locations =
            CompletionQuery.getIds(client, "https://connect.squareup.com/v2/locations", "locations",
                "id");
        completionCache.store(serviceDefinition().shortName(), "locations", locations);
      } else {
        locations = surveysOpt.get();
      }

      urlList = urlList.replace("location", locations, true);
    }

    return CompletableFuture.completedFuture(urlList.matchingUrls(prefix));
  }

  @Override public Collection<? extends String> hosts() {
    return SquareUpUtil.API_HOSTS;
  }
}
