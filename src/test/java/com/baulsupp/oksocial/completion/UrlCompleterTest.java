package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.services.test.TestAuthInterceptor;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlCompleterTest {
  private List<AuthInterceptor<?>> services = Lists.newArrayList(new TestAuthInterceptor());

  private UrlCompleter completer =
      new UrlCompleter(services, new OkHttpClient(), CredentialsStore.NONE,
          CompletionVariableCache.NONE);

  @Test public void returnsAllUrls() throws IOException {
    assertEquals(
        new UrlList(UrlList.Match.HOSTS,
            Lists.newArrayList("https://test.com", "https://test.com/", "https://api1.test.com",
                "https://api1.test.com/")),
        completer.urlList(""));
  }

  @Test public void returnsMatchingUrls() throws IOException {
    assertEquals(
        Lists.newArrayList("https://api1.test.com", "https://api1.test.com/"),
        completer.urlList("https://api1").getUrls("https://api1"));
    assertEquals(Lists.newArrayList(), completer.urlList("https://api2").getUrls("https://api2"));
  }

  @Test public void returnsMatchingEndpointUrls() throws IOException {
    assertEquals(Lists.newArrayList("https://api1.test.com/users.json",
        "https://api1.test.com/usersList.json"),
        completer.urlList("https://api1.test.com/u").getUrls("https://api1.test.com/u"));
  }
}
