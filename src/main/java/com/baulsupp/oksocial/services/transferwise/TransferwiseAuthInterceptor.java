package com.baulsupp.oksocial.services.transferwise;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://api-docs.transferwise.com/docs/versions/v1/overview
 */
public class TransferwiseAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition(host(), "Transferwise API", "transferwise");
  }

  protected String host() {
    return "api.transferwise.com";
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
    System.err.println("Authorising Transferwise API");

    String clientId =
        Secrets.prompt("Transferwise Client Id", "transferwise.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Transferwise Client Secret", "transferwise.clientSecret", "", true);

    return TransferwiseAuthFlow.login(client, outputHandler, host(), clientId, clientSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        TransferwiseUtil.apiRequest("/v1/me", requestBuilder), fieldExtractor("name")).validate(
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

    RequestBody body =
        new FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", credentials.refreshToken.get())
            .build();
    String basic = Credentials.basic(credentials.clientId.get(), credentials.clientSecret.get());
    Request request =
        new Request.Builder().url("https://" + host() + "/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }

  @Override public Set<String> hosts() {
    return Sets.newHashSet(host());
  }
}
