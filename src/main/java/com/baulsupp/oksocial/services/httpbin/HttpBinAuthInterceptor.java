package com.baulsupp.oksocial.services.httpbin;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * http://httpbin.org/
 */
public class HttpBinAuthInterceptor implements AuthInterceptor<BasicCredentials> {

  @Override public Response intercept(Interceptor.Chain chain, BasicCredentials credentials)
      throws IOException {
    Request request = chain.request();

    request =
        request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
            .build();

    return chain.proceed(request);
  }

  @Override public BasicCredentials authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    String user =
        Secrets.prompt("User", "httpbin.user", "", false);
    String password =
        Secrets.prompt("Password", "httpbin.password", "", true);

    return new BasicCredentials(user, password);
  }

  @Override public ServiceDefinition<BasicCredentials> serviceDefinition() {
    return new BasicAuthServiceDefinition("httpbin.org", "HTTP Bin", "httpbin",
        "https://httpbin.org/", null);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, BasicCredentials credentials) throws IOException {
    return CompletableFuture.completedFuture(
        Optional.of(new ValidatedCredentials(credentials.user, null)));
  }

  @Override public Collection<String> hosts() {
    return Collections.unmodifiableSet(Sets.newHashSet(
        "httpbin.org")
    );
  }
}
