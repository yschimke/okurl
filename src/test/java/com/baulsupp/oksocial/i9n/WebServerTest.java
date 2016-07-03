package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.security.CertificatePin;
import com.google.common.collect.Lists;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebServerTest {
  @Rule public MockWebServer server = new MockWebServer();
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();

  {
    main.outputHandler = output;
  }

  private SslClient sslClient = SslClient.localhost();

  @Test public void httpsRequestInsecureFails() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.enqueue(new MockResponse().setBody("Isla Sorna"));

    main.arguments = Lists.newArrayList(server.url("/").toString());

    main.run();

    assertEquals(0, output.responses.size());
    assertEquals(1, output.failures.size());
    assertTrue(output.failures.get(0) instanceof SSLHandshakeException);
  }

  @Test public void httpsRequestInsecure() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.enqueue(new MockResponse().setBody("Isla Sorna"));

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.allowInsecure = true;

    main.run();

    assertEquals(1, output.responses.size());
    assertEquals(200, output.responses.get(0).code());
  }

  @Test @Ignore public void httpsRequestSecure() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.enqueue(new MockResponse().setBody("Isla Sorna"));

    main.arguments = Lists.newArrayList(server.url("/").toString());

    main.run();

    assertEquals(1, output.responses.size());
    assertEquals(200, output.responses.get(0).code());
  }

  @Test public void rejectedWithPin() throws Exception {
    server.useHttps(sslClient.socketFactory, false);
    server.enqueue(new MockResponse().setBody("Isla Sorna"));

    main.arguments = Lists.newArrayList(server.url("/").toString());
    main.certificatePins = Lists.newArrayList(new CertificatePin(server.getHostName() + ":" +
        "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="));
    main.allowInsecure = true;

    main.run();

    assertEquals(0, output.responses.size());
    assertEquals(1, output.failures.size());
    assertTrue(output.failures.get(0) instanceof SSLPeerUnverifiedException);
  }
}
