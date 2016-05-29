/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baulsupp.oksocial.authenticator;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * OAuth 2.0 verification code receiver that runs a Jetty server on a free port, waiting for a
 * redirect with the verification code. <p> Implementation is thread-safe. </p>
 *
 * @author Yaniv Inbar
 * @since 1.11
 */
public final class LocalServer implements VerificationCodeReceiver {
  private final String field;
  /**
   * Server or {@code null} before {@link #getRedirectUri()}.
   */
  private Server server;

  /**
   * Verification code or {@code null} for none.
   */
  String code;

  /**
   * Error code or {@code null} for none.
   */
  String error;

  /**
   * Condition for receiving an authorization response.
   */
  final CountDownLatch gotAuthorizationResponse = new CountDownLatch(1);

  /**
   * Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
   */
  private int port;

  /**
   * Host name to use.
   */
  private final String host;

  public LocalServer() {
    this("localhost", -1);
  }

  public LocalServer(String host, int port) {
    this.host = host;
    this.port = port;
    this.field = "code";
  }

  public LocalServer(String host, int port, String field) {
    this.host = host;
    this.port = port;
    this.field = field;
  }

  @Override
  public String getRedirectUri() throws IOException {
    if (port == -1) {
      port = getUnusedPort();
    }
    server = new Server(port);
    for (Connector c : server.getConnectors()) {
      c.setHost(host);
    }
    server.addHandler(new CallbackHandler());
    try {
      server.start();
    } catch (Exception e) {
      Throwables.propagateIfPossible(e);
      throw new IOException(e);
    }
    return "http://" + host + ":" + port + "/callback";
  }

  @Override
  public String waitForCode() throws IOException {
    try {
      while (code == null && error == null) {
        gotAuthorizationResponse.await(1, TimeUnit.MINUTES);
      }
      if (error != null) {
        throw new IOException("User authorization failed (" + error + ")");
      }
      return code;
    } catch (InterruptedException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void stop() throws IOException {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        Throwables.propagateIfPossible(e);
        throw new IOException(e);
      }
      server = null;
    }
  }

  /**
   * Returns the host name to use.
   *
   * @return host name
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
   *
   * @return specific port or -1
   */
  public int getPort() {
    return port;
  }

  private static int getUnusedPort() throws IOException {
    Socket s = new Socket();
    s.bind(null);
    try {
      return s.getLocalPort();
    } finally {
      s.close();
    }
  }

  /**
   * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it
   * where {@link #waitForCode} will find it.
   */
  class CallbackHandler extends AbstractHandler {

    @Override
    public void handle(
        String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException {
      writeLandingHtml(response);
      response.flushBuffer();
      ((Request) request).setHandled(true);
      error = request.getParameter("error");
      code = request.getParameter(field);
      gotAuthorizationResponse.countDown();
    }

    private void writeLandingHtml(HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");

      PrintWriter doc = response.getWriter();
      doc.println("<html>");
      doc.println("<head><title>Authentication Token Received</title></head>");
      doc.println("<body>");
      doc.println("Received verification code. You may now close this window...");
      doc.println("</body>");
      doc.println("</HTML>");
      doc.flush();
    }
  }
}