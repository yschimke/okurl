package com.baulsupp.oksocial.services.linkedin;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

public class LinkedinAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.linkedin.com", "Linkedin API", "linkedin",
        "https://developer.linkedin.com/docs/rest-api", "https://www.linkedin.com/developer/apps");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    Request.Builder requestBuilder =
        request.newBuilder().addHeader("Authorization", "Bearer " + token);

    if (request.url().queryParameter("format") == null && request.header("x-li-format") == null) {
      requestBuilder.addHeader("x-li-format", "json");
    }

    return chain.proceed(requestBuilder.build());
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Linkedin API");

    String clientId =
        Secrets.prompt("Linkedin Client Id", "linkedin.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Linkedin Client Secret", "linkedin.clientSecret", "", true);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "linkedin.scopes",
            Arrays.asList("r_basicprofile", "r_emailaddress", "rw_company_admin", "w_share"));

    return LinkedinAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        LinkedinUtil.apiRequest("/v1/people/~:(formatted-name)", requestBuilder),
        fieldExtractor("formattedName")).validate(client);
  }

  @Override public Collection<String> hosts() {
    return LinkedinUtil.API_HOSTS;
  }
}
