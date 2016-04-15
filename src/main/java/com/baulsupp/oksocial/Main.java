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

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.twitter.TwitterCachingInterceptor;
import com.baulsupp.oksocial.twitter.TwitterDeflatedResponseInterceptor;
import com.google.common.collect.Sets;
import com.moczul.ok2curl.CurlInterceptor;
import io.airlift.airline.*;
import okhttp3.*;
import okhttp3.internal.framed.Http2;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Command(name = Main.NAME, description = "A curl for social apis.")
public class Main extends HelpOption implements Runnable {
  private static Logger logger = Logger.getLogger(Main.class.getName());

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
  public boolean followRedirects = false;

  @Option(name = {"-k", "--insecure"},
      description = "Allow connections to SSL sites without certs")
  public boolean allowInsecure = false;

  @Option(name = {"-i", "--include"}, description = "Include protocol headers in the output")
  public boolean showHeaders = false;

  @Option(name = "--frames", description = "Log HTTP/2 frames to STDERR")
  public boolean showHttp2Frames = false;

  @Option(name = "--debug", description = "Debug")
  public boolean debug = false;

  @Option(name = {"-e", "--referer"}, description = "Referer URL")
  public String referer;

  @Option(name = {"-V", "--version"}, description = "Show version number and quit")
  public boolean version = false;

  @Option(name = {"--cache"}, description = "Cache directory")
  public File cacheDirectory = null;

  @Option(name = {"--protocols"}, description = "Protocols")
  public String protocols;

  @Option(name = {"-o", "--output"}, description = "Output file/directory")
  public File outputDirectory;

  @Option(name = {"--authorize"}, description = "Authorize API")
  public boolean authorize;

  @Option(name = {"--token"}, description = "Use existing Token for authorization")
  public String token;

  @Option(name = {"--curl"}, description = "Show curl commands")
  public boolean curl = false;

  @Option(name = {"--dns"}, description = "IP Preferences", allowedValues = {"system", "ipv4",
      "ipv6", "ipv4only", "ipv6only"})
  public String ipmode = "system";

  @Option(name = {"--clientcert"}, description = "Send Client Certificate")
  public File clientCert = null;

  @Option(name = {"--opensc"}, description = "Send OpenSC Client Certificate")
  public boolean opensc = false;

  @Option(name = {"--socks"}, description = "Use SOCKS proxy")
  public InetAddress socksProxy;

  @Option(name = {"--show-credentials"}, description = "Show Credentials")
  public boolean showCredentials = false;

  @Option(name = {"--alias-names"}, description = "Show Alias Names")
  public boolean aliasNames = false;

  @Arguments(title = "urls", description = "Remote resource URLs")
  public List<String> urls = new ArrayList<>();

  private ServiceInterceptor serviceInterceptor = new ServiceInterceptor();

  private String versionString() {
    return Util.versionString("/oksocial-version.properties");
  }

  @Override
  public void run() {
    configureLogging();

    if (showHelpIfRequested()) {
      return;
    }

    if (version) {
      System.out.println(NAME + " " + versionString());
      System.out.println("OkHttp " + Util.versionString("/okhttp-version.properties"));
      return;
    }

    OutputHandler outputHandler;
    if (outputDirectory != null) {
      outputHandler = new DownloadHandler(outputDirectory);
    } else {
      outputHandler =
          new com.baulsupp.oksocial.ConsoleHandler(showHeaders, true);
    }

    if (showCredentials) {
      for (AuthInterceptor a : serviceInterceptor.services()) {
        printKnownCredentials(a);
      }

      return;
    }

    if (aliasNames) {
      Set<String> names = Sets.newTreeSet();

      for (AuthInterceptor a : serviceInterceptor.services()) {
        names.addAll(a.aliasNames());
      }

      for (String alias : names) {
        System.out.println(alias);
      }

      return;
    }

    try {
      if (authorize) {
        authorize();
      } else {
        OkHttpClient client = null;

        try {
          client = createClient();

          for (String url : urls) {
            logger.log(Level.FINE, "url " + url);

            if (urls.size() > 1) {
              System.err.println(url);
            }

            makeRequest(outputHandler, client, url);
          }
        } finally {
          if (client != null) {
            client.connectionPool().evictAll();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  private void makeRequest(OutputHandler outputHandler, OkHttpClient client, String url) {
    try {
      String alias = getAlias();
      AuthInterceptor auth = mapAlias(alias);

      if (auth != null) {
        url = auth.mapUrl(alias, url);
      }

      Request request = createRequest(url);

      logger.log(Level.FINE, "Request " + request);

      Response response = client.newCall(request).execute();

      outputHandler.showOutput(response);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void authorize() throws Exception {
    AuthInterceptor<?> auth = mapAlias(getAlias());

    if (auth == null && !urls.isEmpty()) {
      auth = mapAlias(urls.get(0));
    }

    if (auth == null) {
      if (urls.size() > 1) {
        throw new RuntimeException("authorize expecting a single url");
      }

      HttpUrl parsedUrl = HttpUrl.parse(urls.get(0));

      for (AuthInterceptor a : serviceInterceptor.services()) {
        if (a.supportsUrl(parsedUrl)) {
          auth = a;
        }
      }
    }

    if (auth == null) {
      throw new RuntimeException("unable to find authenticator");
    }

    if (token == null) {
      authRequest(auth);
    } else {
      storeCredentials(auth);
    }
  }

  private <T> void storeCredentials(AuthInterceptor<T> auth) {
    T credentials = auth.credentialsStore().getServiceDefinition().parseCredentialsString(token);
    auth.credentialsStore().storeCredentials(credentials);
  }

  private <T> void authRequest(AuthInterceptor<T> auth) throws Exception {
    OkHttpClient client = createClient();

    try {
      OkHttpClient.Builder b = client.newBuilder();
      b.networkInterceptors().removeIf(ServiceInterceptor.class::isInstance);
      client = b.build();

      auth.authorize(client);
    } finally {
      if (client != null) {
        client.connectionPool().evictAll();
      }
    }
  }

  private <T> void printKnownCredentials(AuthInterceptor<T> a) {
    T credentials = a.credentials();
    ServiceDefinition<T> sd = a.credentialsStore().getServiceDefinition();
    String credentialsString =
        credentials != null ? sd.formatCredentialsString(credentials) : "None";
    System.out.println(sd.apiHost() + " " + credentialsString);
  }

  private AuthInterceptor mapAlias(String alias) {
    for (AuthInterceptor a : serviceInterceptor.services()) {
      if (a.aliasNames().contains(alias)) {
        return a;
      }
    }

    return null;
  }

  private String getAlias() {
    return System.getProperty("command.name", "oksocial");
  }

  private OkHttpClient createClient() throws Exception {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.followSslRedirects(followRedirects);
    if (connectTimeout != DEFAULT_TIMEOUT) {
      builder.connectTimeout(connectTimeout, SECONDS);
    }
    if (readTimeout != DEFAULT_TIMEOUT) {
      builder.readTimeout(readTimeout, SECONDS);
    }

    X509TrustManager trustManager = null;
    KeyManager[] keyManagers = null;

    if (allowInsecure) {
      trustManager = new InsecureTrustManager();
      builder.hostnameVerifier(new InsecureHostnameVerifier());
    }

    if (clientCert != null) {
      char[] password = System.console().readPassword("keystore password: ");
      keyManagers =
          createLocalKeyManagers(clientCert, password);
    } else if (opensc) {
      char[] password = System.console().readPassword("smartcard password: ");
      keyManagers = OpenSCUtil.getKeyManagers(password);
    }

    builder.dns(DnsSelector.byName(ipmode));

    if (keyManagers != null || trustManager != null) {
      if (trustManager == null) {
        TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
      }

      builder.sslSocketFactory(
          createSslSocketFactory(keyManagers, new TrustManager[]{trustManager}),
          trustManager);
    }

    if (cacheDirectory != null) {
      builder.cache(new Cache(cacheDirectory, 64 * 1024 * 1024));
    }

    configureApiInterceptors(builder);

    if (debug) {
      HttpLoggingInterceptor logging =
          new HttpLoggingInterceptor();
      logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
      builder.networkInterceptors().add(logging);
    }

    if (socksProxy != null) {
      builder.proxy(new Proxy(Proxy.Type.SOCKS, socksProxy.address));
    }

    List<Protocol> requestProtocols = buildProtocols();
    if (requestProtocols != null) {
      builder.protocols(requestProtocols);
    }

    return builder.build();
  }

  private void configureApiInterceptors(OkHttpClient.Builder builder) {
    builder.addNetworkInterceptor(new TwitterCachingInterceptor());

    builder.addNetworkInterceptor(serviceInterceptor);

    builder.addNetworkInterceptor(new TwitterDeflatedResponseInterceptor());

    if (curl) {
      builder.addNetworkInterceptor(new CurlInterceptor(System.err::println));
    }
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

  private Request createRequest(String url) {
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

  private static SSLSocketFactory createSslSocketFactory(KeyManager[] keyManagers,
                                                         TrustManager[] trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext context = SSLContext.getInstance("TLS");

    context.init(keyManagers, trustManagers, null);

    return context.getSocketFactory();
  }

  private static KeyManager[] createLocalKeyManagers(File keystore, char[] password)
      throws Exception {
    KeyStore keystore_client = KeyStore.getInstance("JKS");
    keystore_client.load(new FileInputStream(keystore), password);
    KeyManagerFactory kmf =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keystore_client, password);
    return kmf.getKeyManagers();
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
        @Override
        public String format(LogRecord record) {
          return String.format("%s%n", record.getMessage());
        }
      });
      logger.addHandler(handler);
    }
  }
}
