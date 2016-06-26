package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.services.twilio.TwilioUtil;
import com.baulsupp.oksocial.services.twitter.twurlrc.TwurlrcImport;
import com.baulsupp.oksocial.util.UsageException;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.stream.Collectors.joining;

public class TwitterAuthInterceptor implements AuthInterceptor<TwitterCredentials> {

  @Override public ServiceDefinition<TwitterCredentials> serviceDefinition() {
    return new TwitterServiceDefinition();
  }

  public boolean supportsUrl(HttpUrl url) {
    return TwitterUtil.TWITTER_API_HOSTS.contains(url.host());
  }

  @Override
  public Response intercept(Interceptor.Chain chain, TwitterCredentials credentials)
      throws IOException {
    Request request = chain.request();

    String authHeader = new Signature().generateAuthorization(request, credentials);
    request = request.newBuilder().addHeader("Authorization", authHeader).build();

    return chain.proceed(request);
  }

  @Override public TwitterCredentials authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments)
      throws IOException {
    System.err.println("Authorising Twitter API");

    if (!authArguments.isEmpty() && authArguments.get(0).equals("--twurlrc")) {
      return TwurlrcImport.authorize(authArguments);
    }

    if (authArguments.equals(Lists.newArrayList("--pin"))) {
      String consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false);
      String consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true);

      return new PinAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret);
    }

    if (!authArguments.isEmpty()) {
      throw new UsageException(
          "unexpected arguments to --authorize twitter: " + authArguments.stream()
              .collect(joining(" ")));
    }

    String consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false);
    String consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true);

    return new WebAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, TwitterCredentials credentials) throws IOException {
    return new JsonCredentialsValidator(
        TwitterUtil.apiRequest("/1.1/account/verify_credentials.json", requestBuilder),
        map -> (String) map.get("name")).validate(client);
  }

  @Override public Collection<? extends String> completions(String url, boolean hosts) {
    return TwilioUtil.API_HOSTS;
  }
}
