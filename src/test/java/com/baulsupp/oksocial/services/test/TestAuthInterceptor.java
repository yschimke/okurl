package com.baulsupp.oksocial.services.test;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TestAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    return chain.proceed(chain.request());
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments)
      throws IOException {
    if (authArguments.isEmpty()) {
      return new Oauth2Token("testToken");
    } else {
      return new Oauth2Token(authArguments.get(0));
    }
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return completedFuture(Optional.empty());
  }

  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("localhost", "Test Service", "test",
        "https://docs.test.com");
  }

  @Override public Collection<String> hosts() {
    return Arrays.asList("test.com", "api1.test.com");
  }

  @Override public ApiDocPresenter apiDocPresenter(String url) {
    return (url1, outputHandler, client) -> outputHandler.info("Test: " + url1);
  }
}
