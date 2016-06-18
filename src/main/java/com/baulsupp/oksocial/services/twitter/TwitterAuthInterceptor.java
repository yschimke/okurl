package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.services.twitter.twurlrc.TwurlCredentialsStore;
import com.baulsupp.oksocial.util.UsageException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TwitterAuthInterceptor implements AuthInterceptor<TwitterCredentials> {

  @Override public ServiceDefinition<TwitterCredentials> serviceDefinition() {
    return new TwitterServiceDefinition();
  }

  public boolean supportsUrl(HttpUrl url) {
    return TwitterUtil.TWITTER_API_HOSTS.contains(url.host());
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<TwitterCredentials> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String authHeader = new Signature().generateAuthorization(request, credentials.get());
      request = request.newBuilder().addHeader("Authorization", authHeader).build();
    }

    return chain.proceed(request);
  }

  @Override
  public TwitterCredentials authorize(OkHttpClient client, List<String> authArguments)
      throws IOException {
    System.err.println("Authorising Twitter API");

    if (!authArguments.isEmpty() && authArguments.get(0).equals("--twurlrc")) {
      TwurlCredentialsStore twurlStore;

      if (authArguments.size() > 1) {
        twurlStore = new TwurlCredentialsStore(new File(authArguments.get(1)));
      } else {
        twurlStore = TwurlCredentialsStore.TWURL_STORE;
      }

      Optional<TwitterCredentials> credentials =
          twurlStore.readCredentials();

      if (!credentials.isPresent()) {
        throw new UsageException("No credentials found in " + twurlStore.getFile());
      }

      return credentials.get();
    }

    if (!authArguments.isEmpty()) {
      throw new UsageException(
          "unexpected arguments to --authorize twitter: " + authArguments.stream().collect(
              Collectors.joining(" ")));
    }

    String consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false);
    String consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true);

    return PinAuthorisationFlow.authorise(client, consumerKey, consumerSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, TwitterCredentials credentials) throws IOException {
    return new JsonCredentialsValidator(
        TwitterUtil.apiRequest("/1.1/account/verify_credentials.json", requestBuilder),
        map -> (String) map.get("name")).validate(client);
  }
}
