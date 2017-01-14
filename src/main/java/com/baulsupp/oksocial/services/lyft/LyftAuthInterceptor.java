package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://developer.lyft.com/docs/authentication
 */
public class LyftAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.lyft.com", "Lyft API", "lyft",
        "https://developer.lyft.com/docs");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    request =
        request.newBuilder().addHeader("Authorization", "Bearer " + token).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Lyft API");

    String clientId =
        Secrets.prompt("Lyft Client Id", "lyft.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Lyft Client Secret", "lyft.clientSecret", "", true);

    if (authArguments.equals(Lists.newArrayList("--client"))) {
      return LyftClientAuthFlow.login(client, clientId, clientSecret);
    } else {
      Set<String> scopes =
          Secrets.promptArray("Scopes", "lyft.scopes", LyftUtil.SCOPES);

      return LyftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
    }
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        LyftUtil.apiRequest("/v1/profile", requestBuilder), fieldExtractor("id")).validate(
        client);
  }

  @Override public boolean canRenew(Response result, Oauth2Token credentials) {
    return result.code() == 401
        && credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override
  public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {

    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"refresh_token\", \"refresh_token\": \""
            + credentials.refreshToken.get() + "\"}");
    String basic = Credentials.basic(credentials.clientId.get(), credentials.clientSecret.get());
    Request request =
        new Request.Builder().url("https://api.lyft.com/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        credentials.refreshToken.get(), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }

  @Override public Set<String> hosts() {
    return LyftUtil.API_HOSTS;
  }
}
