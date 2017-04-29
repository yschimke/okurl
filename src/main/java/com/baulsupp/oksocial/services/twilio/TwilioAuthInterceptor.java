package com.baulsupp.oksocial.services.twilio;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.secrets.Secrets;
import com.google.common.collect.Lists;
import com.baulsupp.oksocial.output.OutputHandler;
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

public class TwilioAuthInterceptor implements AuthInterceptor<BasicCredentials> {
  @Override public BasicAuthServiceDefinition serviceDefinition() {
    return new BasicAuthServiceDefinition("api.twilio.com", "Twilio API", "twilio",
        "https://www.twilio.com/docs/api/rest", "https://www.twilio.com/console");
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

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    Optional<UrlList> urlList =
        UrlList.fromResource(name());

    Optional<BasicCredentials> credentials =
        credentialsStore.readDefaultCredentials(serviceDefinition());

    BaseUrlCompleter completer = new BaseUrlCompleter(urlList.get(), hosts());

    credentials.ifPresent(basicCredentials -> completer.withVariable("AccountSid",
        Lists.newArrayList(basicCredentials.user)));

    return completer;
  }

  @Override public Collection<String> hosts() {
    return TwilioUtil.API_HOSTS;
  }
}
