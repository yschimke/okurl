package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://developer.lyft.com/docs/authentication
 */
public class LyftAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.lyft.com", "Lyft API", "lyft");
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
    return LyftUtil.API_HOSTS.contains(url.host());
  }

  @Override
  public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Lyft API");

    String clientId =
        Secrets.prompt("Lyft Client Id", "lyft.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Lyft Client Secret", "lyft.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "lyft.scopes", LyftUtil.SCOPES);

    return LyftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        LyftUtil.apiRequest("/v1/profile", requestBuilder), fieldExtractor("id")).validate(
        client);
  }
}
