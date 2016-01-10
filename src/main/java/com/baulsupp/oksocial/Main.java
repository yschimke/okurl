/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baulsupp.oksocial;

import com.baulsupp.oksocial.twitter.CredentialsStore;
import com.baulsupp.oksocial.twitter.OSXCredentialStore;
import com.baulsupp.oksocial.twitter.PinAuthorisationFlow;
import com.baulsupp.oksocial.twitter.TwitterAuthInterceptor;
import com.baulsupp.oksocial.twitter.TwitterCachingInterceptor;
import com.baulsupp.oksocial.twitter.TwitterCredentials;
import com.baulsupp.oksocial.twitter.TwurlCredentialsStore;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.HelpOption;
import io.airlift.command.Option;
import io.airlift.command.SingleCommand;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Http2;

import static java.util.concurrent.TimeUnit.SECONDS;

@Command(name = Main.NAME, description = "A curl for social apis.")
public class Main extends HelpOption implements Runnable {
  static final String NAME = "oksocial";
  static final int DEFAULT_TIMEOUT = -1;

  static Main fromArgs(String... args) {
    return SingleCommand.singleCommand(Main.class).parse(args);
  }

  public static void main(String... args) {
    fromArgs(args).run();
  }

  @Option(name = {"-X", "--request"}, description = "Specify request command to use")
  public String method;

  @Option(name = {"-d", "--data"}, description = "HTTP POST data")
  public String data;

  @Option(name = {"-H", "--header"}, description = "Custom header to pass to server")
  public List<String> headers;

  @Option(name = {"-A", "--user-agent"}, description = "User-Agent to send to server")
  public String userAgent = NAME + "/" + versionString();

  @Option(name = "--connect-timeout", description = "Maximum time allowed for connection (seconds)")
  public int connectTimeout = DEFAULT_TIMEOUT;

  @Option(name = "--read-timeout", description = "Maximum time allowed for reading data (seconds)")
  public int readTimeout = DEFAULT_TIMEOUT;

  @Option(name = {"-L", "--location"}, description = "Follow redirects")
  public boolean followRedirects;

  @Option(name = {"-k", "--insecure"},
      description = "Allow connections to SSL sites without certs")
  public boolean allowInsecure;

  @Option(name = {"-i", "--include"}, description = "Include protocol headers in the output")
  public boolean showHeaders;

  @Option(name = "--frames", description = "Log HTTP/2 frames to STDERR")
  public boolean showHttp2Frames;

  @Option(name = "--debug", description = "Debug")
  public boolean debug;

  @Option(name = {"-e", "--referer"}, description = "Referer URL")
  public String referer;

  @Option(name = {"-V", "--version"}, description = "Show version number and quit")
  public boolean version;

  @Option(name = {"--cache"}, description = "Cache directory")
  public File cacheDirectory = new File(System.getProperty("user.home"), ".oksocial.cache");

  @Option(name = {"--protocols"}, description = "Protocols")
  private String protocols;

  @Arguments(title = "urls", description = "Remote resource URLs")
  public List<String> urls;

  private OkHttpClient client;

  private String versionString() {
    return Util.versionString("/oksocial-version.properties");
  }

  @Override public void run() {
    configureLogging();

    if (showHelpIfRequested()) {
      return;
    }
    if (version) {
      System.out.println(NAME + " " + versionString());
      System.out.println("OkHttp " + Util.versionString("/okhttp-version.properties"));
      return;
    }

    OutputHandler outputHandler =
        new com.baulsupp.oksocial.ConsoleHandler(showHeaders, true);

    client = createClient();
    try {
      for (String url : urls) {
        if (urls.size() > 1) {
          System.err.println(url);
        }

        try {
          if (url.equals("authorize")) {
            System.err.println("Authorising");
            TwitterCredentials newCredentials =
                PinAuthorisationFlow.authorise(client, TwitterAuthInterceptor.TEST_CREDENTIALS);

            updateCredentials(newCredentials);
            client = TwitterAuthInterceptor.updateCredentials(client, newCredentials);
          } else {
            Request request = createRequest(url);
            Response response = client.newCall(request).execute();

            outputHandler.showOutput(response);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } finally {
      client.connectionPool().evictAll();
    }
  }

  private OkHttpClient createClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.followSslRedirects(followRedirects);
    if (connectTimeout != DEFAULT_TIMEOUT) {
      builder.connectTimeout(connectTimeout, SECONDS);
    }
    if (readTimeout != DEFAULT_TIMEOUT) {
      builder.readTimeout(readTimeout, SECONDS);
    }
    if (allowInsecure) {
      builder.sslSocketFactory(createInsecureSslSocketFactory());
      builder.hostnameVerifier(createInsecureHostnameVerifier());
    }

    builder.cache(new Cache(cacheDirectory, 64 * 1024 * 1024));

    configureApiInterceptors(builder);

    List<Protocol> requestProtocols = buildProtocols();
    if (requestProtocols != null) {
      builder.protocols(requestProtocols);
    }

    return builder.build();
  }

  private void configureApiInterceptors(OkHttpClient.Builder builder) {
    TwitterCredentials credentials = null;
    try {
      credentials = readCredentials();

      if (credentials != null) {
        builder.networkInterceptors().add(new TwitterAuthInterceptor(credentials));
      }

      builder.networkInterceptors().add(new TwitterCachingInterceptor());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read twitter credentials", e);
    }
  }

  private TwitterCredentials readCredentials()
      throws IOException {
    CredentialsStore nativeCredentialsStore = buildNativeCredentialsStore();

    TwitterCredentials credentials = null;

    if (nativeCredentialsStore != null) {
      credentials = nativeCredentialsStore.readDefaultCredentials();
    }

    if (credentials == null) {
      TwurlCredentialsStore twurlCredentialsStore =
          new TwurlCredentialsStore(new File(System.getProperty("user.home"), ".twurlrc"));

      credentials = twurlCredentialsStore.readDefaultCredentials();

      if (credentials != null && nativeCredentialsStore != null) {
        nativeCredentialsStore.storeCredentials(credentials);
      }
    }

    return credentials;
  }

  private void updateCredentials(TwitterCredentials newCredentials) throws IOException {
    CredentialsStore nativeCredentialsStore = buildNativeCredentialsStore();

    if (nativeCredentialsStore != null) {
      nativeCredentialsStore.storeCredentials(newCredentials);
    }
  }

  private OSXCredentialStore buildNativeCredentialsStore() {
    return Util.isOSX() ? new OSXCredentialStore() : null;
  }

  private List<Protocol> buildProtocols() {
    if (protocols != null) {
      List<Protocol> protocolValues = new ArrayList<>();

      try {
        for (String protocol : protocols.split(",")) {
          protocolValues.add(Protocol.get(protocol));
        }
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }

      if (!protocolValues.contains(Protocol.HTTP_1_1)) {
        protocolValues.add(Protocol.HTTP_1_1);
      }

      return protocolValues;
    } else {
      return null;
    }
  }

  private String getRequestMethod() {
    if (method != null) {
      return method;
    }
    if (data != null) {
      return "POST";
    }
    return "GET";
  }

  private RequestBody getRequestBody() {
    if (data == null) {
      return null;
    }
    String bodyData = data;

    String mimeType = "application/x-www-form-urlencoded";
    if (headers != null) {
      for (String header : headers) {
        String[] parts = header.split(":", -1);
        if ("Content-Type".equalsIgnoreCase(parts[0])) {
          mimeType = parts[1].trim();
          headers.remove(header);
          break;
        }
      }
    }

    return RequestBody.create(MediaType.parse(mimeType), bodyData);
  }

  Request createRequest(String url) {
    Request.Builder request = new Request.Builder();

    request.url(url);
    request.method(getRequestMethod(), getRequestBody());

    if (headers != null) {
      for (String header : headers) {
        String[] parts = header.split(":", 2);
        request.header(parts[0], parts[1]);
      }
    }
    if (referer != null) {
      request.header("Referer", referer);
    }
    request.header("User-Agent", userAgent);

    return request.build();
  }

  private static SSLSocketFactory createInsecureSslSocketFactory() {
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      TrustManager permissive = new X509TrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
      };
      context.init(null, new TrustManager[] {permissive}, null);
      return context.getSocketFactory();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static HostnameVerifier createInsecureHostnameVerifier() {
    return (s, sslSession) -> true;
  }

  private void configureLogging() {
    if (debug) {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.ALL);
      Logger.getLogger("").addHandler(handler);
      Logger.getLogger("").setLevel(Level.ALL);
    } else if (showHttp2Frames) {
      Logger logger = Logger.getLogger(Http2.class.getName() + "$FrameLogger");
      logger.setLevel(Level.FINE);
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.FINE);
      handler.setFormatter(new SimpleFormatter() {
        @Override public String format(LogRecord record) {
          return String.format("%s%n", record.getMessage());
        }
      });
      logger.addHandler(handler);
    }
  }
}
