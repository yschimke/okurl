package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebServerTest {
  @Rule public MockWebServer server = new MockWebServer();
  private Main main = new Main();
  private SslClient sslClient = SslClient.localhost();

  @Test public void httpsRequestInsecure() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.enqueue(new MockResponse().setBody("Isla Sorna"));

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.allowInsecure = true;

    OkHttpClient client = main.createClientBuilder().build();

    Request.Builder requestBuilder = main.createRequestBuilder();

    Request request = requestBuilder.url(server.url("/")).build();

    try (Response response = client.newCall(request).execute()) {
      assertEquals(200, response.code());
    }
  }
}
