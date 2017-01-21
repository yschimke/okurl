package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionMappings;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;
import static java.util.stream.Collectors.toSet;

/**
 * https://developer.google.com/docs/authentication
 */
public class GoogleAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  private Set<String> hosts = null;
  private DiscoveryIndex discoveryIndex;

  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("www.googleapis.com", "Google API", "google",
        "https://developers.google.com/");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    request =
        request.newBuilder().addHeader("Authorization", "Bearer " + token).build();

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    String host = url.host();

    return GoogleUtil.API_HOSTS.contains(host) || host.endsWith(".googleapis.com");
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Google API");

    String clientId =
        Secrets.prompt("Google Client Id", "google.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Google Client Secret", "google.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "google.scopes", GoogleUtil.SCOPES);

    return GoogleAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        requestBuilder.url("https://www.googleapis.com/oauth2/v3/userinfo").build(),
        fieldExtractor("name")).validate(client);
  }

  @Override public boolean canRenew(Oauth2Token credentials) {
    return credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override
  public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {
    RequestBody body =
        new FormBody.Builder().add("client_id", credentials.clientId.get())
            .add("refresh_token", credentials.refreshToken.get())
            .add("client_secret", credentials.clientSecret.get())
            .add("grant_type", "refresh_token")
            .build();

    Request request =
        new Request.Builder().url("https://www.googleapis.com/oauth2/v4/token")
            .post(body)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        credentials.refreshToken.get(), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }

  @Override public synchronized Collection<String> hosts() throws IOException {
    if (hosts == null) {
      Optional<UrlList> urlList = UrlList.fromResource(name());

      hosts = urlList.get().getUrls("").stream().map(this::extractHost).collect(toSet());
    }

    return hosts;
  }

  private <R> String extractHost(String s) {
    return HttpUrl.parse(s).host();
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    if (isPastHost(prefix)) {
      List<String> discoveryPaths = DiscoveryIndex.loadStatic().getDiscoveryUrlForPrefix(prefix);

      GoogleDiscoveryCompleter completer =
          GoogleDiscoveryCompleter.forApis(DiscoveryRegistry.instance(client),
              discoveryPaths);

      return completer;
    } else {
      UrlList urlList = UrlList.fromResource(name()).get();

      return new BaseUrlCompleter(urlList, hosts());
    }
  }

  private boolean isPastHost(String prefix) {
    return prefix.matches("https://.*/.*");
  }

  @Override public ApiDocPresenter apiDocPresenter(String url) throws IOException {
    return new DiscoveryApiDocPresenter(discoveryIndex());
  }

  private synchronized DiscoveryIndex discoveryIndex() throws IOException {
    if (discoveryIndex == null) {
      discoveryIndex = DiscoveryIndex.loadStatic();
    }

    return discoveryIndex;
  }
}
