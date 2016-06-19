package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import java.util.logging.LogManager;
import okhttp3.Protocol;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

public class LoggingTest {
  @Rule public MockWebServer server = new MockWebServer();
  private Main main = new Main();

  private SslClient sslClient = SslClient.localhost();

  @AfterClass
  public static void resetLogging() {
    LogManager.getLogManager().reset();
  }

  @Test public void logsData() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.setProtocols(Lists.newArrayList(Protocol.HTTP_2, Protocol.HTTP_1_1));
    server.enqueue(new MockResponse().setBody("Isla Sorna"));
    main.allowInsecure = true;

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.debug = true;

    main.run();
  }
}