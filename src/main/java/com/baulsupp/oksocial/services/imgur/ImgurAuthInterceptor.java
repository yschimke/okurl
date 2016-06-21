package com.baulsupp.oksocial.services.imgur;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.services.imgur.ImgurUtil.apiRequest;

public class ImgurAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.imgur.com", "Imgur API", "imgur");
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
    return ImgurUtil.API_HOSTS.contains(url.host());
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Imgur API");

    String clientId =
        Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true);

    return ImgurAuthFlow.login(client, outputHandler, clientId, clientSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        apiRequest("/3/account/me", requestBuilder),
        map -> getName(map)).validate(client);
  }

  private String getName(Map<String, Object> map) {
    Map<String, Object> data = (Map<String, Object>) map.get("data");

    return (String) data.get("url");
  }

  @Override public boolean canRenew(Response result, Oauth2Token credentials) {
    return result.code() == 403
        && credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override
  public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {
    RequestBody body =
        new FormBody.Builder().add("refresh_token", credentials.refreshToken.get())
            .add("client_id", credentials.clientId.get())
            .add("client_secret", credentials.clientSecret.get())
            .add("grant_type", "refresh_token")
            .build();
    Request request = new Request.Builder().url("https://api.imgur.com/oauth2/token")
        .method("POST", body)
        .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }
}
