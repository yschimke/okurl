package com.baulsupp.oksocial.services.twilio;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.services.UrlList;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.stream.Collectors.toList;

public class TwilioAuthInterceptor implements AuthInterceptor<BasicCredentials> {
  @Override public ServiceDefinition<BasicCredentials> serviceDefinition() {
    return new TwilioServiceDefinition();
  }

  @Override
  public Response intercept(Interceptor.Chain chain, BasicCredentials credentials)
      throws IOException {
    Request request = chain.request();

    request =
        request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
            .build();

    return chain.proceed(request);
  }

  @Override public BasicCredentials authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) {
    String user =
        Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false);
    String password =
        Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true);

    return new BasicCredentials(user, password);
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, BasicCredentials credentials) throws IOException {
    return new JsonCredentialsValidator(
        TwilioUtil.apiRequest("/2010-04-01/Accounts.json", requestBuilder),
        this::getName).validate(client);
  }

  private String getName(Map<String, Object> map) {
    List<Map<String, Object>> accounts = (List<Map<String, Object>>) map.get("accounts");

    return (String) accounts.get(0).get("friendly_name");
  }

  @Override public List<String> matchingUrls(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore)
      throws IOException {
    UrlList urls = UrlList.fromResource("twilio").get();

    Optional<BasicCredentials> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());
    if (credentials.isPresent()) {
      List<String> u = urls.getUrls();

      String sid = credentials.get().user;

      urls = new UrlList(u.stream().map(s -> s.replace("{AccountSid}", sid)).collect(toList()));
    }

    return urls.matchingUrls(prefix);
  }

  @Override public Collection<? extends String> hosts() {
    return TwilioUtil.API_HOSTS;
  }
}
