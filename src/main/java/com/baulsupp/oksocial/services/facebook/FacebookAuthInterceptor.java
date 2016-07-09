package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.CompletionCache;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.util.ClientException;
import com.baulsupp.oksocial.util.JsonUtil;
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

import static com.baulsupp.oksocial.services.facebook.FacebookUtil.apiRequest;

public class FacebookAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("graph.facebook.com", "Facebook API", "facebook");
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
    System.err.println("Authorising Facebook API");

    String clientId = Secrets.prompt("Facebook App Id", "facebook.appId", "", false);
    String clientSecret = Secrets.prompt("Facebook App Secret", "facebook.appSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "facebook.scopes",
            Arrays.asList("public_profile", "user_friends", "email"));

    if (scopes.contains("all")) {
      scopes.remove("all");
      scopes.addAll(FacebookUtil.ALL_PERMISSIONS);
    }

    return FacebookAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  private String extract(Map<String, Object> map) {
    return "" + map.get("name") + " (" + map.get("id") + ")";
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(apiRequest("/me", requestBuilder), this::extract,
        apiRequest("/app", requestBuilder), this::extract).validate(client);
  }

  @Override public Collection<? extends String> hosts() {
    return FacebookUtil.API_HOSTS;
  }

  @Override public Future<List<String>> matchingUrls(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionCache completionCache, boolean expensive)
      throws IOException {
    if (!expensive) {
      return AuthInterceptor.super.matchingUrls(prefix, client, credentialsStore, completionCache, expensive);
    } else {
      HttpUrl url = HttpUrl.parse(prefix);

      FacebookMetadata metadata = getMetadata(client, url);

      System.out.println(metadata.connections());

      if (!url.encodedPath().endsWith("/")) {

      } else {

      }

      return null;
    }
  }

  private FacebookMetadata getMetadata(OkHttpClient client, HttpUrl url) throws IOException {
    url = url.newBuilder().addQueryParameter("metadata", "1").build();
    Request request = new Request.Builder().url(url).build();

    Map<String, Object> result = AuthUtil.makeJsonMapRequest(client, request);

    return new FacebookMetadata((Map<String, Object>) result.get("metadata"));
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.initialise();

    main.urlCompletion = "https://graph.facebook.com/me/";
    main.run();
  }
}
