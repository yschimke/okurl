package com.baulsupp.oksocial.services.test;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class TestAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public boolean supportsUrl(HttpUrl url) {
    return url.host().equals("localhost");
  }

  @Override public Response intercept(Interceptor.Chain chain, Optional<Oauth2Token> credentials)
      throws IOException {
    return chain.proceed(chain.request());
  }

  @Override public Oauth2Token authorize(OkHttpClient client, List<String> authArguments)
      throws IOException {
    if (authArguments.isEmpty()) {
      return new Oauth2Token("testToken");
    } else {
      return new Oauth2Token(authArguments.get(0));
    }
  }

  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("localhost", "Test Service", "test");
  }
}
