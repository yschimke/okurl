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
import com.baulsupp.oksocial.authenticator.PrintCredentials;
import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.commands.CommandRegistry;
import com.baulsupp.oksocial.commands.OksocialCommand;
import com.baulsupp.oksocial.commands.ShellCommand;
import com.baulsupp.oksocial.dns.DnsOverride;
import com.baulsupp.oksocial.dns.DnsSelector;
import com.baulsupp.oksocial.services.twitter.TwitterCachingInterceptor;
import com.baulsupp.oksocial.services.twitter.TwitterDeflatedResponseInterceptor;
import com.baulsupp.oksocial.util.InetAddress;
import com.baulsupp.oksocial.util.InsecureHostnameVerifier;
import com.baulsupp.oksocial.util.InsecureTrustManager;
import com.baulsupp.oksocial.util.OkHttpResponseFuture;
import com.baulsupp.oksocial.util.OpenSCUtil;
import com.baulsupp.oksocial.util.Util;
import com.google.api.client.util.Lists;
import com.google.common.collect.Sets;
import com.moczul.ok2curl.CurlInterceptor;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Http2;
import okhttp3.logging.HttpLoggingInterceptor;

import static java.util.concurrent.TimeUnit.SECONDS;

@Command(name = Main.NAME, description = "A curl for social apis.")
public class Main extends HelpOption implements Runnable {
  private static Logger logger = Logger.getLogger(Main.class.getName());

  static final String NAME = "oksocial";
  private static final int DEFAULT_TIMEOUT = -1;

  // store active logger to avoid GC
  private Logger activeLogger;

  private static Main fromArgs(String... args) {
    return SingleCommand.singleCommand(Main.class).parse(args);
  }

  public static void main(String... args) {
    fromArgs(args).run();
  }

  @Option(name = {"-X", "--request"}, description = "Specify request command to use")
  private String method;

  @Option(name = {"-d", "--data"}, description = "HTTP POST data")
  private String data;

  @Option(name = {"-H", "--header"}, description = "Custom header to pass to server")
  private List<String> headers;

  @Option(name = {"-A", "--user-agent"}, description = "User-Agent to send to server")
  private String userAgent = NAME + "/" + versionString();

  @Option(name = "--connect-timeout", description = "Maximum time allowed for connection (seconds)")
  private int connectTimeout = DEFAULT_TIMEOUT;

  @Option(name = "--read-timeout", description = "Maximum time allowed for reading data (seconds)")
  private int readTimeout = DEFAULT_TIMEOUT;

  @Option(name = {"-L", "--location"}, description = "Follow redirects")
  private boolean followRedirects = false;

  @Option(name = {"-k", "--insecure"},
      description = "Allow connections to SSL sites without certs")
  private boolean allowInsecure = false;

  @Option(name = {"-i", "--include"}, description = "Include protocol headers in the output")
  private boolean showHeaders = false;

  @Option(name = "--frames", description = "Log HTTP/2 frames to STDERR")
  private boolean showHttp2Frames = false;

  @Option(name = "--debug", description = "Debug")
  private boolean debug = false;

  @Option(name = {"-e", "--referer"}, description = "Referer URL")
  private String referer;

  @Option(name = {"-V", "--version"}, description = "Show version number and quit")
  private boolean version = false;

  @Option(name = {"--cache"}, description = "Cache directory")
  private File cacheDirectory = null;

  @Option(name = {"--protocols"}, description = "Protocols")
  private String protocols;

  @Option(name = {"-o", "--output"}, description = "Output file/directory")
  private File outputDirectory;

  @Option(name = {"--authorize"}, description = "Authorize API")
  private boolean authorize;

  @Option(name = {"--token"}, description = "Use existing Token for authorization")
  private String token;

  @Option(name = {"--curl"}, description = "Show curl commands")
  private boolean curl = false;

  @Option(name = {"--dns"}, description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)",
      allowedValues = {"system", "ipv4", "ipv6", "ipv4only", "ipv6only"})
  private String ipmode = "system";

  @Option(name = {"--resolve"}, description = "DNS Overrides (HOST:TARGET)")
  private String resolve = null;

  @Option(name = {"--clientcert"}, description = "Send Client Certificate")
  private File clientCert = null;

  @Option(name = {"--opensc"}, description = "Send OpenSC Client Certificate")
  private boolean opensc = false;

  @Option(name = {"--socks"}, description = "Use SOCKS proxy")
  private InetAddress socksProxy;

  @Option(name = {"--show-credentials"}, description = "Show Credentials")
  private boolean showCredentials = false;

  @Option(name = {"--alias-names"}, description = "Show Alias Names")
  private boolean aliasNames = false;

  @Arguments(title = "urls", description = "Remote resource URLs")
  private List<String> urls = new ArrayList<>();

  private ServiceInterceptor serviceInterceptor = new ServiceInterceptor();

  private CommandRegistry commandRegistry = new CommandRegistry();

  private String versionString() {
    return Util.versionString("/oksocial-version.properties");
  }

  private List<OkHttpClient> clients = Lists.newArrayList();

  @Override
  public void run() {
    configureLogging();

    if (showHelpIfRequested()) {
      return;
    }

    try {

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
        PrintCredentials.printKnownCredentials(build(createClientBuilder()),
            createRequestBuilder(),
            serviceInterceptor.services());

        return;
      }

      if (aliasNames) {
        printAliasNames();
        return;
      }

      if (authorize) {
        authorize();
      } else {
        executeRequests(outputHandler);
      }
    } catch (UsageException e) {
      System.err.println(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      clients.forEach(client -> {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
      });
    }
  }

  private void executeRequests(OutputHandler outputHandler) throws Exception {
    OkHttpClient.Builder clientBuilder = createClientBuilder();

    ShellCommand command = getShellCommand();

    Request.Builder requestBuilder = createRequestBuilder();

    List<Request> requests = command.buildRequests(clientBuilder, requestBuilder, urls);

    if (requests.isEmpty()) {
      throw new UsageException("no urls specified");
    }

    OkHttpClient client = build(clientBuilder);

    List<Future<Response>> responseFutures = enqueueRequests(requests, client);
    processResponses(outputHandler, responseFutures);
  }

  private void processResponses(OutputHandler outputHandler, List<Future<Response>> responseFutures)
      throws IOException, InterruptedException {
    // TODO allow setting failure/cancel strategy
    boolean failed = false;
    for (Future<Response> responseFuture : responseFutures) {
      if (failed) {
        responseFuture.cancel(true);
      } else {
        try {
          outputHandler.showOutput(responseFuture.get());
        } catch (ExecutionException ee) {
          ee.getCause().printStackTrace();

          failed = true;
        }
      }
    }
  }

  private List<Future<Response>> enqueueRequests(List<Request> requests, OkHttpClient client) {
    List<Future<Response>> responseFutures = Lists.newArrayList();

    for (Request request : requests) {
      logger.log(Level.FINE, "url " + request.url());

      if (requests.size() > 1) {
        System.err.println(request.url());
      }

      responseFutures.add(makeRequest(client, request));
    }
    return responseFutures;
  }

  private OkHttpClient build(OkHttpClient.Builder clientBuilder) {
    OkHttpClient client = clientBuilder.build();

    clients.add(client);

    return client;
  }

  private ShellCommand getShellCommand()
      throws Exception {
    String commandName = getCommandName();

    return commandRegistry.getCommandByName(commandName).orElse(new OksocialCommand());
  }

  private void printAliasNames() {
    Set<String> names = Sets.newTreeSet(commandRegistry.names());

    names.forEach(System.out::println);
  }

  private Future<Response> makeRequest(OkHttpClient client, Request request) {
    logger.log(Level.FINE, "Request " + request);

    Call call = client.newCall(request);

    OkHttpResponseFuture result = new OkHttpResponseFuture(call);

    call.enqueue(result);

    return result.future;
  }

  private void authorize() throws Exception {
    ShellCommand command = getShellCommand();

    Optional<AuthInterceptor<?>> auth =
        command.authenticator().flatMap(authName -> serviceInterceptor.getByName(authName));

    if (!auth.isPresent() && !urls.isEmpty()) {
      auth = serviceInterceptor.getByName(urls.get(0));

      if (!auth.isPresent()) {
        auth = serviceInterceptor.getByUrl(urls.get(0));
      }
    }

    if (!auth.isPresent()) {
      throw new UsageException(
          "unable to find authenticator. Specify name from " + serviceInterceptor.names()
              .stream()
              .collect(
                  Collectors.joining(", ")));
    } else {
      if (token == null) {
        authRequest(auth.get());
      } else {
        storeCredentials(auth.get());
      }
    }
  }

  private <T> void storeCredentials(AuthInterceptor<T> auth) {
    T credentials = auth.credentialsStore().getServiceDefinition().parseCredentialsString(token);
    auth.credentialsStore().storeCredentials(credentials);
  }

  private <T> void authRequest(AuthInterceptor<T> auth) throws Exception {
    OkHttpClient client = build(createClientBuilder());

    OkHttpClient.Builder b = client.newBuilder();
    b.networkInterceptors().removeIf(ServiceInterceptor.class::isInstance);
    client = build(b);

    auth.authorize(client);
  }

  private String getCommandName() {
    return System.getProperty("command.name", "oksocial");
  }

  public OkHttpClient.Builder createClientBuilder() throws Exception {
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

    Dns dns = DnsSelector.byName(ipmode);
    if (resolve != null) {
      dns = DnsOverride.build(dns, resolve);
    }
    builder.dns(dns);

    if (keyManagers != null || trustManager != null) {
      if (trustManager == null) {
        TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
      }

      builder.sslSocketFactory(
          createSslSocketFactory(keyManagers, new TrustManager[] {trustManager}),
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

    return builder;
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

  public Request.Builder createRequestBuilder() {
    Request.Builder request = new Request.Builder();

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

    return request;
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
      activeLogger = Logger.getLogger("");
      activeLogger.addHandler(handler);
      activeLogger.setLevel(Level.ALL);
    } else if (showHttp2Frames) {
      activeLogger = Logger.getLogger(Http2.class.getName() + "$FrameLogger");
      activeLogger.setLevel(Level.FINE);
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.FINE);
      handler.setFormatter(new SimpleFormatter() {
        @Override
        public String format(LogRecord record) {
          return String.format("%s%n", record.getMessage());
        }
      });
      activeLogger.addHandler(handler);
    }
  }
}
