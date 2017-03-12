package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.secrets.Secrets;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
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

import static com.baulsupp.oksocial.services.stackexchange.StackExchangeUtil.apiRequest;

public class StackExchangeAuthInterceptor implements AuthInterceptor<StackExchangeToken> {
  @Override public StackExchangeServiceDefinition serviceDefinition() {
    return new StackExchangeServiceDefinition();
  }

  @Override public Response intercept(Interceptor.Chain chain, StackExchangeToken credentials)
      throws IOException {
    Request request = chain.request();

    HttpUrl newUrl = request.url()
        .newBuilder()
        .addQueryParameter("access_token", credentials.accessToken)
        .addQueryParameter("key", credentials.key)
        .build();

    request =
        request.newBuilder().url(newUrl).build();

    return chain.proceed(request);
  }

  private String extract(Map<String, Object> map) {
    List<Map<String, Object>> items = ((List<Map<String, Object>>) map.get("items"));

    if (items.size() > 0) {
      return "" + items.get(0).get("display_name");
    } else {
      return "Unknown";
    }
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, StackExchangeToken credentials) throws IOException {
    return new JsonCredentialsValidator(apiRequest("/2.2/me?site=drupal", requestBuilder),
        this::extract).validate(client);
  }

  @Override public StackExchangeToken authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising StackExchange API");

    String clientId =
        Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false);
    String clientSecret =
        Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret", "", true);
    String clientKey =
        Secrets.prompt("StackExchange Key", "stackexchange.key", "", false);
    Set<String> scopes =
        Secrets.promptArray("Scopes", "stackexchange.scopes", StackExchangeUtil.SCOPES);

    return StackExchangeAuthFlow.login(client, outputHandler, clientId, clientSecret, clientKey,
        scopes);
  }

  @Override public Collection<String> hosts() {
    return StackExchangeUtil.API_HOSTS;
  }
}
