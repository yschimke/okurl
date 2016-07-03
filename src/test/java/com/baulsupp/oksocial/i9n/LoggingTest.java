package com.baulsupp.oksocial.i9n;

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

import static org.junit.Assert.assertEquals;

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

  @Test public void version() throws Exception {
    TestOutputHandler output = new TestOutputHandler();

    main.outputHandler = output;
    main.version = true;

    main.run();

    assertEquals(0, output.failures.size());
  }
}
