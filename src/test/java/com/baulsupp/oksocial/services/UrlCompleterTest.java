package com.baulsupp.oksocial.services;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.services.test.TestAuthInterceptor;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;
import org.junit.Test;

import static java.util.regex.Pattern.quote;
import static org.junit.Assert.assertEquals;

public class UrlCompleterTest {
  private List<AuthInterceptor<?>> services = Lists.newArrayList(new TestAuthInterceptor());
  private CredentialsStore credentialsStore = new CredentialsStore() {
    @Override
    public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
      return Optional.empty();
    }

    @Override
    public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    }
  };

  private UrlCompleter completer = new UrlCompleter(services, new OkHttpClient(), credentialsStore,
      CompletionVariableCache.NONE);

  @Test public void returnsAllUrls() throws IOException {
    assertEquals(
        new UrlList(quote(""), Lists.newArrayList("https://test.com/", "https://api1.test.com/")),
        completer.urlList(""));
  }

  @Test public void returnsMatchingUrls() throws IOException {
    assertEquals(
        Lists.newArrayList("https://api1.test.com/"), completer.urlList("https://api1").getUrls("https://api1"));
    assertEquals(Lists.newArrayList(), completer.urlList("https://api2").getUrls("https://api2"));
  }

  @Test public void returnsMatchingEndpointUrls() throws IOException {
    assertEquals(Lists.newArrayList("https://api1.test.com/users.json",
        "https://api1.test.com/usersList.json"),
        completer.urlList("https://api1.test.com/u").getUrls("https://api1.test.com/u"));
  }
}
