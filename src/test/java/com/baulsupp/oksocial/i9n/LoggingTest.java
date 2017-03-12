package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import ee.schimke.oksocial.output.TestOutputHandler;
import java.util.logging.LogManager;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ExternalResourceSupport.class)
public class LoggingTest {
  @Rule
  public MockWebServer server = new MockWebServer();

  private Main main = new Main();

  private SslClient sslClient = SslClient.localhost();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();

  {
    main.outputHandler = output;
  }

  @AfterAll
  public static void resetLogging() {
    LogManager.getLogManager().reset();
  }

  @Test
  public void logsData() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.setProtocols(Lists.newArrayList(Protocol.HTTP_2, Protocol.HTTP_1_1));
    server.enqueue(new MockResponse().setBody("Isla Sorna"));
    main.allowInsecure = true;

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.debug = true;

    main.run();
  }

  @Test
  public void version() throws Exception {
    TestOutputHandler<Response> output = new TestOutputHandler<Response>();

    main.version = true;

    main.run();

    assertEquals(0, output.failures.size());
  }
}
