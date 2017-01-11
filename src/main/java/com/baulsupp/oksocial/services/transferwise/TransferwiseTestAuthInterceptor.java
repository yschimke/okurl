package com.baulsupp.oksocial.services.transferwise;

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;

import static java.util.stream.Collectors.toList;

public class TransferwiseTestAuthInterceptor extends TransferwiseAuthInterceptor {
  @Override protected String host() {
    return "test-restgw.transferwise.com";
  }

  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition(host(), "Transferwise Test API", "transferwise-test",
        "https://api-docs.transferwise.com/");
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    UrlList urlList = UrlList.fromResource("transferwise").get();

    List<String> testUrls = urlList.getUrls("")
        .stream()
        .map(s -> s.replace("api.transferwise.com", host()))
        .collect(toList());

    return new BaseUrlCompleter(new UrlList(UrlList.Match.SITE, testUrls), hosts());
  }
}
