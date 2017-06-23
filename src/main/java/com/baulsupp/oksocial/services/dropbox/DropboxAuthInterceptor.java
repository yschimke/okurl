package com.baulsupp.oksocial.services.dropbox;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://developer.dropbox.com/docs/authentication
 */
public class DropboxAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("api.dropboxapi.com", "Dropbox API", "dropbox",
        "https://www.dropbox.com/developers", "https://www.dropbox.com/developers/apps");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    Request.Builder builder =
        request.newBuilder().addHeader("Authorization", "Bearer " + token);
    request = builder.build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Dropbox API");

    String clientId =
        Secrets.prompt("Dropbox Client Id", "dropbox.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Dropbox Client Secret", "dropbox.clientSecret", "", true);

    return DropboxAuthFlow.login(client, outputHandler, clientId, clientSecret);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    RequestBody body = FormBody.create(MediaType.parse("application/json"), "null");
    return new JsonCredentialsValidator(
        DropboxUtil.apiRequest("/2/users/get_current_account", requestBuilder)
            .newBuilder()
            .post(body)
            .build(),
        fieldExtractor("email")).validate(client);
  }

  @Override public Set<String> hosts() {
    return DropboxUtil.API_HOSTS;
  }
}
