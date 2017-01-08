package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.util.LoggingUtil;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class DiscoveryCompleterMain {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    LoggingUtil.configureLogging(true, true);

    OkHttpClient client = new OkHttpClient();

    GoogleDiscoveryCompleter completer = GoogleDiscoveryCompleter.forApis(
        client, newArrayList("https://www.googleapis.com/discovery/v1/apis/urlshortener/v1/rest"));

    UrlList r =
        completer.siteUrls(HttpUrl.parse("https://www.googleapis.com/urlshortener/v1/url")).get();

    List<String> urls = r.getUrls("https://www.googleapis.com/urlshortener/v1/url");

    System.out.println(urls);

    assertEquals(newArrayList("https://www.googleapis.com/urlshortener/v1/url",
        "https://www.googleapis.com/urlshortener/v1/url/history"),
        urls);

    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
  }
}
