package com.baulsupp.oksocial;

import brave.Tracer;
import brave.Tracing;
import brave.http.HttpTracing;
import brave.internal.Platform;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import com.baulsupp.oksocial.apidocs.ServiceApiDocPresenter;
import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.Authorisation;
import com.baulsupp.oksocial.authenticator.PrintCredentials;
import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.commands.CommandRegistry;
import com.baulsupp.oksocial.commands.MainAware;
import com.baulsupp.oksocial.commands.OksocialCommand;
import com.baulsupp.oksocial.commands.ShellCommand;
import com.baulsupp.oksocial.completion.ArgumentCompleter;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.TmpCompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.FixedTokenCredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.credentials.PreferencesCredentialsStore;
import com.baulsupp.oksocial.jjs.JavascriptApiCommand;
import com.baulsupp.oksocial.location.BestLocation;
import com.baulsupp.oksocial.location.LocationSource;
import com.baulsupp.oksocial.network.DnsMode;
import com.baulsupp.oksocial.network.DnsOverride;
import com.baulsupp.oksocial.network.DnsSelector;
import com.baulsupp.oksocial.network.GoogleDns;
import com.baulsupp.oksocial.network.IPvMode;
import com.baulsupp.oksocial.network.InterfaceSocketFactory;
import com.baulsupp.oksocial.network.NettyDns;
import com.baulsupp.oksocial.okhttp.OkHttpResponseExtractor;
import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture;
import com.baulsupp.oksocial.output.ConsoleHandler;
import com.baulsupp.oksocial.output.DownloadHandler;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.output.ResponseExtractor;
import com.baulsupp.oksocial.output.util.PlatformUtil;
import com.baulsupp.oksocial.output.util.UsageException;
import com.baulsupp.oksocial.security.*;
import com.baulsupp.oksocial.services.twitter.TwitterCachingInterceptor;
import com.baulsupp.oksocial.services.twitter.TwitterDeflatedResponseInterceptor;
import com.baulsupp.oksocial.tracing.UriTransportRegistry;
import com.baulsupp.oksocial.tracing.ZipkinConfig;
import com.baulsupp.oksocial.tracing.ZipkinTracingInterceptor;
import com.baulsupp.oksocial.tracing.ZipkinTracingListener;
import com.baulsupp.oksocial.util.*;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mcdermottroe.apple.OSXKeychainException;
import com.moczul.ok2curl.CurlInterceptor;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.net.Proxy;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.IOUtils;
import zipkin.Span;
import zipkin.reporter.Reporter;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
@Command(name = Main.NAME, description = "A curl for social apis.")
public class Main extends HelpOption {
  private static Logger logger = Logger.getLogger(Main.class.getName());

  static final String NAME = "oksocial";

  private static Main fromArgs(String... args) {
    return SingleCommand.singleCommand(Main.class).parse(args);
  }

  public static void main(String... args) {
    int result = fromArgs(args).run();
    System.exit(result);
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
  public Integer connectTimeout;

  @Option(name = "--read-timeout", description = "Maximum time allowed for reading data (seconds)")
  public Integer readTimeout;

  @Option(name = {"--no-follow"}, description = "Follow redirects")
  public boolean dontFollowRedirects = false;

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

  @Option(name = {"--renew"}, description = "Renew API Authorization")
  public boolean renew;

  @Option(name = {"--token"}, description = "Use existing Token for authorization")
  public String token;

  @Option(name = {"--curl"}, description = "Show curl commands")
  public boolean curl = false;

  @Option(name = {"--zipkin", "-z"}, description = "Activate Zipkin Tracing")
  public boolean zipkin = false;

  @Option(name = {"--zipkinTrace"}, description = "Activate Detailed Zipkin Tracing")
  public boolean zipkinTrace = false;

  @Option(name = {"--ip"}, description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)",
      allowedValues = {"system", "ipv4", "ipv6", "ipv4only", "ipv6only"})
  public IPvMode ipMode = IPvMode.SYSTEM;

  @Option(name = {"--dns"}, description = "DNS (netty, java)",
      allowedValues = {"java", "netty"})
  public DnsMode dnsMode = DnsMode.JAVA;

  @Option(name = {"--dnsServers"}, description = "Specific DNS Servers (csv, google)")
  public String dnsServers = null;

  @Option(name = {"--resolve"}, description = "DNS Overrides (HOST:TARGET)")
  public List<String> resolve = null;

  @Option(name = {"--certificatePin"}, description = "Specific Local Network Interface")
  public List<CertificatePin> certificatePins = null;

  @Option(name = {"--networkInterface"}, description = "Specific Local Network Interface")
  public String networkInterface = null;

  @Option(name = {"--clientauth"}, description = "Use Client Authentication (from keystore)")
  public boolean clientAuth = false;

  @Option(name = {"--keystore"}, description = "Keystore")
  public File keystoreFile = null;

  @Option(name = {"--cert"}, description = "Use given server cert (Root CA)")
  public List<File> serverCerts = Lists.newArrayList();

  @Option(name = {"--opensc"}, description = "Send OpenSC Client Certificate (slot)")
  public Integer opensc;

  @Option(name = {"--socks"}, description = "Use SOCKS proxy")
  public InetAddressParam socksProxy;

  @Option(name = {"--proxy"}, description = "Use HTTP proxy")
  public InetAddressParam proxy;

  @Option(name = {"--show-credentials"}, description = "Show Credentials")
  public boolean showCredentials = false;

  @Option(name = {"--alias-names"}, description = "Show Alias Names")
  public boolean aliasNames = false;

  @Option(name = {"-r", "--raw"}, description = "Raw Output")
  public boolean rawOutput = false;

  @Option(name = {"-s", "--set"}, description = "Token Set e.g. work")
  public String tokenSet = null;

  @Option(name = {"--serviceNames"}, description = "Service Names")
  public boolean serviceNames = false;

  @Option(name = {"--urlCompletion"}, description = "URL Completion")
  public boolean urlComplete;

  @Option(name = {"--apidoc"}, description = "API Documentation")
  public boolean apiDoc;

  @Option(name = {"--ssldebug"}, description = "SSL Debug")
  public boolean sslDebug;

  @Option(name = {"--user"}, description = "user:password for basic auth")
  public String user;

  @Option(name = {"--maxrequests"}, description = "Concurrency Level")
  private int maxRequests = 16;

  public String commandName = System.getProperty("command.name", "oksocial");

  public String completionFile = System.getenv("COMPLETION_FILE");

  @Arguments(title = "arguments", description = "Remote resource URLs")
  public List<String> arguments = new ArrayList<>();

  public ServiceInterceptor serviceInterceptor = null;

  private Authorisation authorisation;

  public OkHttpClient client = null;

  public Request.Builder requestBuilder;

  public CommandRegistry commandRegistry = new CommandRegistry();

  public OutputHandler<Response> outputHandler = null;

  public CredentialsStore credentialsStore = null;

  public CompletionVariableCache completionVariableCache;

  public LocationSource locationSource = new BestLocation();

  private NioEventLoopGroup eventLoopGroup;

  private List<Closeable> closeables = Lists.newArrayList();

  private String versionString() {
    return getClass().getPackage().getImplementationVersion();
  }

  public int run() {
    if (sslDebug) {
      System.setProperty("javax.net.debug", "ssl,handshake");
    }

    LoggingUtil.Companion.configureLogging(debug, showHttp2Frames);

    if (outputHandler == null) {
      outputHandler = buildHandler();
    }

    if (showHelpIfRequested()) {
      return 0;
    }

    try {
      if (version) {
        outputHandler.info(NAME + " " + versionString());
        return 0;
      }

      initialise();

      if (showCredentials) {
        new PrintCredentials(client, credentialsStore, outputHandler,
            serviceInterceptor).showCredentials(arguments, this::createRequestBuilder);
        return 0;
      }

      if (aliasNames) {
        printAliasNames();
        return 0;
      }

      if (serviceNames) {
        outputHandler.info(serviceInterceptor.names().stream().collect(joining(" ")));
        return 0;
      }

      if (urlComplete) {
        outputHandler.info(urlCompletionList());
        return 0;
      }

      if (apiDoc) {
        showApiDocs();
        return 0;
      }

      if (authorize) {
        authorize();
        return 0;
      }

      if (renew) {
        renew();
        return 0;
      }

      return executeRequests(outputHandler);
    } catch (UsageException e) {
      outputHandler.showError("error: " + e.getMessage(), null);
      return -1;
    } catch (Exception e) {
      outputHandler.showError("unknown error", e);
      return -2;
    } finally {
      closeClients();
    }
  }

  private void showApiDocs() throws Exception {
    ServiceApiDocPresenter docs =
        new ServiceApiDocPresenter(serviceInterceptor, client, credentialsStore);

    getFullCompletionUrl().ifPresent(u -> {
      try {
        docs.explainApi(u, outputHandler, client);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    });
  }

  // TODO refactor this mess out of Main
  private String urlCompletionList() throws Exception {
    ShellCommand command = getShellCommand();

    Optional<ArgumentCompleter> commandCompletor = command.completer();
    if (commandCompletor.isPresent()) {
      UrlList urls = commandCompletion(commandCompletor.get(), arguments);

      String prefix = arguments.get(arguments.size() - 1);

      if (completionFile != null) {
        urls.toFile(new File(completionFile), 0, prefix);
      }

      return urls.getUrls(prefix).stream().collect(joining("\n"));
    }

    ArgumentCompleter completer =
        new UrlCompleter(serviceInterceptor.services(), client, credentialsStore,
            completionVariableCache);

    Optional<String> fullCompletionUrlOpt = getFullCompletionUrl();

    // reload hack (in case changed for "" case)
    String originalCompletionUrl = arguments.get(arguments.size() - 1);

    if (fullCompletionUrlOpt.isPresent()) {
      String fullCompletionUrl = fullCompletionUrlOpt.get();
      UrlList urls = completer.urlList(fullCompletionUrl);

      final int strip;
      if (!fullCompletionUrl.equals(originalCompletionUrl)) {
        strip = fullCompletionUrl.length() - originalCompletionUrl.length();
      } else {
        strip = 0;
      }

      if (completionFile != null) {
        urls.toFile(new File(completionFile), strip, originalCompletionUrl);
      }

      return urls.getUrls(fullCompletionUrl).stream()
          .map(u -> u.substring(strip))
          .collect(joining("\n"));
    } else {
      return "";
    }
  }

  private UrlList commandCompletion(ArgumentCompleter urlCompleter, List<String> arguments)
      throws IOException {
    return urlCompleter.urlList(arguments.get(arguments.size() - 1));
  }

  /*
   * The last url in arguments which should be used for completion or apidoc requests.
   * In the case of javascript command expansion, it is expanded first before
   * being returned.
   *
   * n.b. arguments may be modified by this call.
   */
  private Optional<String> getFullCompletionUrl() throws Exception {
    if (arguments.isEmpty()) {
      return empty();
    }

    String urlToComplete = arguments.get(arguments.size() - 1);

    ShellCommand command = getShellCommand();

    if (command instanceof JavascriptApiCommand) {
      List<Request> requests = command.buildRequests(client, requestBuilder, arguments);

      if (requests.size() > 0) {
        HttpUrl newUrl = requests.get(0).url();

        // support "" -> http://api.test.com
        if (urlToComplete.isEmpty() && newUrl.encodedPath().equals("/")) {
          urlToComplete = "/";
          arguments.remove(arguments.size() - 1);
          arguments.add(urlToComplete);
        }

        String newUrlCompletion = newUrl.toString();

        if (newUrlCompletion.endsWith(urlToComplete)) {
          return Optional.of(newUrlCompletion);
        }
      }
    } else if (UrlCompleter.Companion.isPossibleAddress(urlToComplete)) {
      return Optional.of(urlToComplete);
    }

    return empty();
  }

  public void initialise() throws Exception {
    if (outputHandler == null) {
      outputHandler = buildHandler();
    }

    if (credentialsStore == null) {
      credentialsStore = createCredentialsStore();
    }

    closeables.add(() -> {
      if (client != null) {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
      }
    });

    OkHttpClient.Builder clientBuilder = createClientBuilder();

    if (user != null) {
      String[] userParts = user.split(":", 2);
      if (userParts.length < 2) {
        throw new UsageException("--user should have user:password");
      }
      String credential = Credentials.basic(userParts[0], userParts[1]);

      clientBuilder.authenticator((route, response) -> {
        logger.fine("Challenges: " + response.challenges());

        // authenticate once
        if (response.request().header("Authorization") != null) {
          return null;
        }

        return response.request().newBuilder()
            .header("Authorization", credential)
            .build();
      });
    }

    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setMaxRequests(maxRequests);
    dispatcher.setMaxRequestsPerHost(maxRequests);
    clientBuilder.dispatcher(dispatcher);

    OkHttpClient authClient = clientBuilder.build();
    serviceInterceptor = new ServiceInterceptor(authClient, credentialsStore);

    authorisation =
        new Authorisation(serviceInterceptor, credentialsStore, authClient, outputHandler);

    clientBuilder.networkInterceptors().add(0, serviceInterceptor);

    if (zipkin || zipkinTrace) {
      applyZipkin(clientBuilder);
    }

    client = clientBuilder.build();

    requestBuilder = createRequestBuilder();

    if (completionVariableCache == null) {
      completionVariableCache = new TmpCompletionVariableCache();
    }
  }

  private void applyZipkin(OkHttpClient.Builder clientBuilder) throws IOException {
    ZipkinConfig config = ZipkinConfig.Companion.load();
    String zipkinSenderUri = config.zipkinSenderUri();
    Reporter<Span> reporter;

    if (zipkinSenderUri != null) {
      reporter = UriTransportRegistry.Companion.forUri(zipkinSenderUri);
    } else {
      reporter = Platform.get();
    }

    Tracing tracing = Tracing.newBuilder()
        .localServiceName("oksocial")
        .reporter(reporter)
        .sampler(Sampler.ALWAYS_SAMPLE)
        .build();

    HttpTracing httpTracing = HttpTracing.create(tracing);

    Tracer tracer = tracing.tracer();

    Consumer<TraceContext> opener =
        tc -> closeables.add(() -> {
          String link = config.openFunction().invoke(tc);

          if (link != null) {
            openLink(link);
          }
        });

    clientBuilder.eventListenerFactory(
        call -> new ZipkinTracingListener(call, tracer, httpTracing, opener, zipkinTrace));

    clientBuilder.addNetworkInterceptor(new ZipkinTracingInterceptor(tracing));

    closeables.add(() -> {
      tracing.close();
      if (reporter instanceof Flushable) {
        ((Flushable) reporter).flush();
      }
      if (reporter instanceof Closeable) {
        ((Closeable) reporter).close();
      }
    });
  }

  private void openLink(String link) {
    try {
      outputHandler.openLink(link);
    } catch (IOException e) {
      outputHandler.showError("Can't open link", e);
    }
  }

  public OkHttpClient getClient() {
    return client;
  }

  private CredentialsStore createCredentialsStore() throws OSXKeychainException {
    if (token != null && !authorize) {
      return new FixedTokenCredentialsStore(token);
    }

    if (PlatformUtil.isOSX()) {
      return new OSXCredentialsStore(tokenSet);
    } else {
      return new PreferencesCredentialsStore(tokenSet);
    }
  }

  private void closeClients() {
    for (Closeable c : closeables) {
      IOUtils.closeQuietly(c);
    }
  }

  private OutputHandler buildHandler() {
    ResponseExtractor responseExtractor = new OkHttpResponseExtractor();

    if (outputDirectory != null) {
      return new DownloadHandler(responseExtractor, outputDirectory);
    } else if (rawOutput) {
      return new DownloadHandler(responseExtractor, new File("-"));
    } else {
      return ConsoleHandler.instance(responseExtractor);
    }
  }

  private int executeRequests(OutputHandler outputHandler) throws Exception {
    ShellCommand command = getShellCommand();

    List<Request> requests = command.buildRequests(client, requestBuilder, arguments);

    if (!command.handlesRequests()) {
      if (requests.isEmpty()) {
        throw new UsageException("no urls specified");
      }

      List<Future<Response>> responseFutures = enqueueRequests(requests, client);
      boolean failed = processResponses(outputHandler, responseFutures);
      return failed ? -5 : 0;
    }

    return 0;
  }

  private boolean processResponses(OutputHandler outputHandler,
      List<Future<Response>> responseFutures)
      throws IOException, InterruptedException {
    boolean failed = false;
    for (Future<Response> responseFuture : responseFutures) {
      if (failed) {
        responseFuture.cancel(true);
      } else {
        try (Response response = responseFuture.get()) {
          showOutput(outputHandler, response);
        } catch (ExecutionException ee) {
          outputHandler.showError("request failed", ee.getCause());
          failed = true;
        }
      }
    }
    return failed;
  }

  private void showOutput(OutputHandler outputHandler, Response response)
      throws IOException {
    if (showHeaders) {
      outputHandler.info(StatusLine.get(response).toString());
      Headers headers = response.headers();
      for (int i = 0, size = headers.size(); i < size; i++) {
        outputHandler.info(headers.name(i) + ": " + headers.value(i));
      }
      outputHandler.info("");
    } else if (!response.isSuccessful()) {
      outputHandler.showError(StatusLine.get(response).toString(), null);
    }

    outputHandler.showOutput(response);
  }

  private List<Future<Response>> enqueueRequests(List<Request> requests, OkHttpClient client) {
    List<Future<Response>> responseFutures = Lists.newArrayList();

    for (Request request : requests) {
      logger.log(Level.FINE, "url " + request.url());

      if (requests.size() > 1 && !debug) {
        System.err.println(request.url());
      }

      responseFutures.add(makeRequest(client, request));
    }
    return responseFutures;
  }

  private ShellCommand getShellCommand() {
    ShellCommand shellCommand =
        commandRegistry.getCommandByName(commandName);

    if (shellCommand == null) {
      shellCommand = new OksocialCommand();
    }

    if (shellCommand instanceof MainAware) {
      ((MainAware) shellCommand).setMain(this);
    }

    return shellCommand;
  }

  private void printAliasNames() {
    Set<String> names = Sets.newTreeSet(commandRegistry.names());

    names.forEach(outputHandler::info);
  }

  private Future<Response> makeRequest(OkHttpClient client, Request request) {
    logger.log(Level.FINE, "Request " + request);

    Call call = client.newCall(request);

    OkHttpResponseFuture result = new OkHttpResponseFuture();

    call.enqueue(result);

    return result.getFuture();
  }

  private void authorize() throws Exception {
    authorisation.authorize(findAuthInterceptor(), ofNullable(token), arguments);
  }

  private void renew() throws Exception {
    authorisation.renew(findAuthInterceptor());
  }

  private @Nullable
  AuthInterceptor<?> findAuthInterceptor() throws Exception {
    ShellCommand command = getShellCommand();

    String authenticator = command.authenticator();
    @Nullable AuthInterceptor<?> auth = null;

    if (authenticator != null) {
      auth = serviceInterceptor.getByName(authenticator);
    }

    if (auth == null && !arguments.isEmpty()) {
      String name = arguments.remove(0);

      auth = serviceInterceptor.findAuthInterceptor(name);
    }
    return auth;
  }

  public OkHttpClient.Builder createClientBuilder() throws Exception {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.followSslRedirects(!dontFollowRedirects);
    builder.followRedirects(!dontFollowRedirects);
    if (connectTimeout != null) {
      builder.connectTimeout(connectTimeout, SECONDS);
    }
    if (readTimeout != null) {
      builder.readTimeout(readTimeout, SECONDS);
    }

    builder.dns(buildDns());

    if (networkInterface != null) {
      builder.socketFactory(getSocketFactory());
    }

    configureTls(builder);

    if (cacheDirectory != null) {
      builder.cache(new Cache(cacheDirectory, 64 * 1024 * 1024));
    }

    // TODO move behind AuthInterceptor API
    builder.addNetworkInterceptor(new TwitterCachingInterceptor());
    builder.addNetworkInterceptor(new TwitterDeflatedResponseInterceptor());

    if (curl) {
      builder.addNetworkInterceptor(new CurlInterceptor(System.err::println));
    }

    if (debug) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::info);
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
      builder.networkInterceptors().add(loggingInterceptor);
    }

    if (socksProxy != null) {
      builder.proxy(new Proxy(Proxy.Type.SOCKS, socksProxy.getAddress()));
    } else if (proxy != null) {
      builder.proxy(new Proxy(Proxy.Type.HTTP, proxy.getAddress()));
    }

    if (protocols != null) {
      builder.protocols(ProtocolUtil.INSTANCE.parseProtocolList(protocols));
    }

    return builder;
  }

  private Dns buildDns() {
    Dns dns;
    if (dnsMode == DnsMode.NETTY) {
      dns = NettyDns.Companion.byName(ipMode, getEventLoopGroup(), dnsServers);
    } else if (dnsMode == DnsMode.DNSGOOGLE) {
      dns = new DnsSelector(ipMode,
          GoogleDns.Companion.fromHosts(() -> Main.this.client, ipMode, "216.58.216.142", "216.239.34.10",
              "2607:f8b0:400a:809::200e"));
    } else {
      if (dnsServers != null) {
        throw new UsageException("unable to set dns servers with java DNS");
      }

      dns = new DnsSelector(ipMode, Dns.SYSTEM);
    }
    if (resolve != null) {
      dns = DnsOverride.Companion.build(dns, resolve);
    }
    return dns;
  }

  private NioEventLoopGroup getEventLoopGroup() {
    if (eventLoopGroup == null) {
      ThreadFactory threadFactory = new DefaultThreadFactory("netty", true);
      eventLoopGroup = new NioEventLoopGroup(5, threadFactory);

      closeables.add(() -> {
        eventLoopGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
      });
    }

    return eventLoopGroup;
  }

  private SocketFactory getSocketFactory() throws SocketException {
    Optional<SocketFactory> socketFactory = InterfaceSocketFactory.Companion.byName(networkInterface);

    if (!socketFactory.isPresent()) {
      throw new UsageException("networkInterface '" + networkInterface + "' not found");
    }

    return socketFactory.get();
  }

  private void configureTls(OkHttpClient.Builder builder) throws Exception {
    ConsoleCallbackHandler callbackHandler = new ConsoleCallbackHandler();

    // possibly null
    KeyStore keystore = null;

    if (keystoreFile != null) {
      keystore = KeystoreUtils.INSTANCE.getKeyStore(keystoreFile);
    }

    List<KeyManager> keyManagers = Lists.newArrayList();

    if (opensc != null) {
      keyManagers.addAll(asList(OpenSCUtil.INSTANCE.getKeyManagers(callbackHandler, opensc)));
    } else if (clientAuth) {
      if (keystore == null) {
        throw new UsageException("--clientauth specified without --keystore");
      }

      keyManagers.add(KeystoreUtils.INSTANCE.createKeyManager(keystore, callbackHandler));
    }

    X509TrustManager trustManager;
    if (allowInsecure) {
      trustManager = new InsecureTrustManager();
      builder.hostnameVerifier(new InsecureHostnameVerifier());
    } else {
      List<X509TrustManager> trustManagers = Lists.newArrayList();

      if (keystore != null) {
        trustManagers.add(CertificateUtils.INSTANCE.trustManagerForKeyStore(keystore));
      }

      if (!serverCerts.isEmpty()) {
        trustManagers.add(CertificateUtils.INSTANCE.load(serverCerts));
      }

      trustManager = CertificateUtils.INSTANCE.combineTrustManagers(trustManagers);
    }

    builder.sslSocketFactory(KeystoreUtils.INSTANCE.createSslSocketFactory(KeystoreUtils.INSTANCE.keyManagerArray(keyManagers), trustManager),
        trustManager);

    if (certificatePins != null) {
      builder.certificatePinner(CertificatePin.Companion.buildFromCommandLine(certificatePins));
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

  private RequestBody getRequestBody(Map<String, String> headerMap) {
    if (data == null) {
      return null;
    }

    String mimeType = "application/x-www-form-urlencoded";

    for (String k : headerMap.keySet()) {
      if ("Content-Type".equalsIgnoreCase(k)) {
        mimeType = headerMap.remove(k);
        break;
      }
    }

    try {
      return RequestBody.create(MediaType.parse(mimeType), FileContent.INSTANCE.readParamBytes(data));
    } catch (IOException e) {
      throw new UsageException(e.getMessage());
    }
  }

  public Request.Builder createRequestBuilder() {
    Request.Builder requestBuilder = new Request.Builder();

    Map<String, String> headerMap = HeaderUtil.INSTANCE.headerMap(headers);

    requestBuilder.method(getRequestMethod(), getRequestBody(headerMap));

    if (headers != null) {
      headerMap.forEach((k, v) -> requestBuilder.header(k, v));
    }
    if (referer != null) {
      requestBuilder.header("Referer", referer);
    }
    requestBuilder.header("User-Agent", userAgent);

    return requestBuilder;
  }
}
