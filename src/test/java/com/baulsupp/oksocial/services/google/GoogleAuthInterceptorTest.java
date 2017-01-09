package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.completion.ApiCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.i9n.TestCredentialsStore;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GoogleAuthInterceptorTest {
  private GoogleAuthInterceptor interceptor = new GoogleAuthInterceptor();
  private OkHttpClient client = new OkHttpClient();
  private CompletionVariableCache cache = CompletionVariableCache.NONE;
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  @Test public void hasManyHosts() throws IOException {
    assertTrue(interceptor.hosts().size() > 10);
    assertTrue(interceptor.hosts().contains("www.googleapis.com"));
    assertTrue(interceptor.hosts().contains("people.googleapis.com"));
  }

  @Test public void supportsAnyGoogleApi() throws IOException {
    assertTrue(interceptor.supportsUrl(HttpUrl.parse("https://altrightfanfiction.googleapis.com")));
  }

  @Test public void completesHosts() throws IOException, ExecutionException, InterruptedException {
    ApiCompleter hostCompleter =
        interceptor.apiCompleter("https://", client, credentialsStore, cache);

    List<String> urls = hostCompleter.prefixUrls().get().getUrls("https://");

    assertTrue(urls.contains("https://www.googleapis.com"));
    assertTrue(urls.contains("https://people.googleapis.com"));
  }

  @Test public void completesWwwPaths()
      throws IOException, ExecutionException, InterruptedException {
    ApiCompleter hostCompleter =
        interceptor.apiCompleter("https://people.googleapis.com", client, credentialsStore, cache);

    List<String> urls = hostCompleter.siteUrls(HttpUrl.parse("https://people.googleapis.com"))
        .get()
        .getUrls("https://people.googleapis.com");

    assertEquals(Lists.newArrayList("https://people.googleapis.com/"), urls);
  }

  // hits the network
  @Test public void completesSitePaths()
      throws IOException, ExecutionException, InterruptedException {
    assumeHasNetwork();

    ApiCompleter hostCompleter =
        interceptor.apiCompleter("https://www.googleapis.com/urlshortener/v1/url", client,
            credentialsStore, cache);

    UrlList urlList =
        hostCompleter.siteUrls(HttpUrl.parse("https://www.googleapis.com/urlshortener/v1/url"))
            .get();

    List<String> urls =
        urlList
            .getUrls("https://www.googleapis.com/urlshortener/v1/url");

    assertEquals(Lists.newArrayList("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"), urls);
  }

  // hits the network
  @Test public void completesSitePathsForDuplicates()
      throws IOException, ExecutionException, InterruptedException {
    assumeHasNetwork();

    ApiCompleter hostCompleter =
        interceptor.apiCompleter("https://www.googleapis.com/", client,
            credentialsStore, cache);

    UrlList urlList =
        hostCompleter.siteUrls(HttpUrl.parse("https://www.googleapis.com/"))
            .get();

    List<String> urls =
        urlList
            .getUrls("https://www.googleapis.com/");

    assertTrue(urls.size() > 5);

    assertTrue(urls.contains("https://www.googleapis.com/urlshortener/v1/url/history"));
    assertTrue(urls.contains("https://www.googleapis.com/oauth2/v1/userinfo"));
  }
}
