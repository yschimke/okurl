package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.security.CertificatePin;
import com.google.common.collect.Lists;
import java.util.logging.LogManager;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    server.enqueue(new MockResponse().setBody("Isla Sorna"));
    main.allowInsecure = true;

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.debug = true;

    main.run();
  }
}
