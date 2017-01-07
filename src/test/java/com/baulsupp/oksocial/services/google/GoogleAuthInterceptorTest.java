package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.completion.ApiCompleter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import okhttp3.HttpUrl;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GoogleAuthInterceptorTest {
  private GoogleAuthInterceptor interceptor = new GoogleAuthInterceptor();

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
        interceptor.apiCompleter("https://", null, null, null);

    List<String> urls = hostCompleter.prefixUrls().get().getUrls("https://");

    assertTrue(urls.contains("https://www.googleapis.com"));
    assertTrue(urls.contains("https://people.googleapis.com"));
  }

  @Test public void completesWwwPaths()
      throws IOException, ExecutionException, InterruptedException {
    ApiCompleter hostCompleter =
        interceptor.apiCompleter("https://www.googleapis.com/", null, null, null);

    List<String> urls = hostCompleter.siteUrls(HttpUrl.parse("https://www.googleapis.com/"))
        .get()
        .getUrls("https://www.googleapis.com/");

    assertTrue(urls.contains("https://www.googleapis.com/youtube/v3/"));
  }
}
