package com.baulsupp.oksocial.services.giphy;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GiphyAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.giphy.com", "Giphy API", "giphy");
  }

  @Override public Optional<Oauth2Token> defaultCredentials() {
    return Optional.of(new Oauth2Token("dc6zaTOxFJmzC"));
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    HttpUrl newUrl = request.url().newBuilder().addQueryParameter("api_key", token).build();

    request =
        request.newBuilder().url(newUrl).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Giphy API");

    String apiKey =
        Secrets.prompt("Giphy API Key", "giphy.apiKey", "", false);

    return new Oauth2Token(apiKey);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return CompletableFuture.completedFuture(Optional.of(new ValidatedCredentials("?", null)));
  }

  @Override public Collection<String> hosts() {
    return GiphyUtil.API_HOSTS;
  }
}
