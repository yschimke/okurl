package com.baulsupp.oksocial.authenticator;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import static java.util.stream.Collectors.joining;

public class SimpleWebServer<T> extends AbstractHandler implements Closeable {
  private int port = 3000;
  private CompletableFuture<T> f = new CompletableFuture<>();
  private Server server;
  private Function<HttpServletRequest, T> codeReader;

  public SimpleWebServer(Function<HttpServletRequest, T> codeReader) throws IOException {
    this.codeReader = codeReader;
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

  public T waitForCode() throws IOException {
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

    if (error != null) {
      out.println(generateFailBody(request, error));
    } else {
      out.println(generateSuccessBody(request));
    }
    out.flush();
    out.close();

    // return response before continuing

    if (error != null) {
      f.completeExceptionally(new IOException(error));
    } else {
      f.complete(codeReader.apply(request));
    }

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
        .map(e -> e.getKey() + " = " + Arrays.asList(e.getValue()).stream().collect(joining(", ")))
        .collect(joining("<br/>"));
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

  public static SimpleWebServer<String> forCode() throws IOException {
    return new SimpleWebServer<>(r -> r.getParameter("code"));
  }

  public static void main(String[] args) throws IOException {
    SimpleWebServer.forCode().waitForCode();
  }

  @Override public void close() {
    shutdown();
  }
}
