package com.baulsupp.oksocial.services.paypal;

import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.BaseUrlCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;

import static java.util.stream.Collectors.toList;

public class PaypalSandboxAuthInterceptor extends PaypalAuthInterceptor {
  @Override protected String shortName() {
    return "paypal-sandbox";
  }

  @Override protected String host() {
    return "api.sandbox.paypal.com";
  }

  @Override public ApiCompleter apiCompleter(String prefix, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache)
      throws IOException {
    UrlList urlList = UrlList.fromResource("paypal").get();

    List<String> testUrls = urlList.getUrls("")
        .stream()
        .map(s -> s.replace("api.paypal.com", host()))
        .collect(toList());

    return new BaseUrlCompleter(new UrlList(UrlList.Match.SITE, testUrls), hosts());
  }
}
