package com.baulsupp.oksocial.services.instagram;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstagramAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.instagram.com", "Instagram API", "instagram");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    HttpUrl newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build();

    request =
        request.newBuilder().url(newUrl).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Instagram API");

    String clientId =
        Secrets.prompt("Instagram Client Id", "instagram.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Instagram Client Secret", "instagram.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "instagram.scopes",
            Arrays.asList("basic", "public_content", "follower_list", "comments", "relationships",
                "likes"));

    return InstagramAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        InstagramUtil.apiRequest("/v1/users/self", requestBuilder),
        this::getName).validate(client);
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> user = (Map<String, Object>) map.get("data");

    return (String) user.get("full_name");
  }

  @Override public Collection<String> hosts() {
    return InstagramUtil.API_HOSTS;
  }
}
