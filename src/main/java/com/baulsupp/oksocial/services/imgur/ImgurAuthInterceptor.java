package com.baulsupp.oksocial.services.imgur;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.services.imgur.ImgurUtil.apiRequest;

public class ImgurAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.imgur.com", "Imgur API", "imgur");
  }

  @Override
  public Response intercept(Interceptor.Chain chain, Optional<Oauth2Token> credentials)
      throws IOException {
    Request request = chain.request();

    if (credentials.isPresent()) {
      String token = credentials.get().accessToken;

      request =
          request.newBuilder().addHeader("Authorization", "Bearer " + token).build();
    }

    return chain.proceed(request);
  }

  public boolean supportsUrl(HttpUrl url) {
    return ImgurUtil.API_HOSTS.contains(url.host());
  }

  @Override
  public Oauth2Token authorize(OkHttpClient client, List<String> authArguments) throws IOException {
    System.err.println("Authorising Imgur API");

    String clientId =
        Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true);

    return ImgurAuthFlow.login(client, clientId, clientSecret);
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
}
