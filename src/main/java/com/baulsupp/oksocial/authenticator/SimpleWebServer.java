package com.baulsupp.oksocial.authenticator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class SimpleWebServer extends AbstractHandler {
  private int port = 3000;
  private CompletableFuture<String> f = new CompletableFuture<String>();
  private Server server;

  public SimpleWebServer() throws IOException {
    org.eclipse.jetty.util.log.Log.initialized();
    org.eclipse.jetty.util.log.Log.setLog(new NullLogger());

    server = new Server(port);
    try {
      server.setHandler(this);
      server.start();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public String getRedirectUri() {
    return "http://localhost:" + port + "/callback";
  }

  public String waitForCode() throws IOException {
    try {
      return f.get(60, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new IOException(e);
    }
  }

  public void handle(String target,
      Request baseRequest,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {
    response.setContentType("text/html; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();

    String error = request.getParameter("error");
    String code = request.getParameter("code");

    if (error != null) {
      out.println(generateFailBody(request, error));
      f.completeExceptionally(new IOException(error));
    } else {
      out.println(generateSuccessBody(request));
      f.complete(code);
    }
    out.flush();
    out.close();

    baseRequest.setHandled(true);

    Thread t = new Thread(() -> shutdown(), "SimpleWebServer Stop");
    t.setDaemon(true);
    t.start();
  }

  private void shutdown() {
    try {
      for (Connector c : getServer().getConnectors()) {
        c.shutdown();
      }
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String generateSuccessBody(HttpServletRequest request) {
    String response = "<html>\n"
        + "<body background=\"http://win.blogadda.com/wp-content/uploads/2015/08/inspire-win-15.jpg\">\n"
        + "<h1>Authorization Token Received!</h1>\n"
        + "</body>\n"
        + "</html>";

    return response;
  }

  private String generateFailBody(HttpServletRequest request, String error) {
    String params = request.getParameterMap()
        .entrySet()
        .stream()
        .map(e -> e.getKey() + " = " + Arrays.asList(e.getValue())
            .stream()
            .collect(Collectors.joining(", "))).collect(Collectors.joining("<br/>"));
    String response = "<html>\n"
        + "<body background=\"http://adsoftheworld.com/sites/default/files/fail_moon_aotw.jpg\">\n"
        + "<h1>Authorization Error!</h1>\n"
        + "<p style=\"font-size: 600%; font-family: Comic Sans, Comic Sans MS, cursive;\">"
        + error
        + "</p>"
        + "<p>"
        + params
        + "</p>"
        + "</body>\n"
        + "</html>";

    return response;
  }

  public static void main(String[] args) throws IOException {
    new SimpleWebServer().waitForCode();
  }
}
