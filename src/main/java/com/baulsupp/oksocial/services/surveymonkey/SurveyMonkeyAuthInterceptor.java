package com.baulsupp.oksocial.services.surveymonkey;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://developer.surveymonkey.com/docs/authentication
 */
public class SurveyMonkeyAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public ServiceDefinition<Oauth2Token> serviceDefinition() {
    return new Oauth2ServiceDefinition("api.surveymonkey.net", "SurveyMonkey API", "surveymonkey");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    request =
        request.newBuilder().addHeader("Authorization", "bearer " + token).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising SurveyMonkey API");

    String clientId =
        Secrets.prompt("SurveyMonkey Client ID", "surveymonkey.clientId", "", false);
    String apiKey =
        Secrets.prompt("SurveyMonkey API Key", "surveymonkey.apiKey", "", false);
    String secret =
        Secrets.prompt("SurveyMonkey Secret", "surveymonkey.secret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "surveymonkey.scopes", SurveyMonkeyUtil.SCOPES);

    return SurveyMonkeyAuthFlow.login(client, outputHandler, clientId, apiKey, secret, scopes);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        SurveyMonkeyUtil.apiRequest("/v1/profile", requestBuilder), fieldExtractor("id")).validate(
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
        new Request.Builder().url("https://api.surveymonkey.com/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }

  @Override public Collection<? extends String> hosts() {
    return SurveyMonkeyUtil.API_HOSTS;
  }
}
