package com.baulsupp.oksocial;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.PrintCredentials;
import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.brave.BaseZipkinHandler;
import com.baulsupp.oksocial.brave.NullZipkinHandler;
import com.baulsupp.oksocial.brave.ServerZipkinHandler;
import com.baulsupp.oksocial.brave.ZipkinHandler;
import com.baulsupp.oksocial.commands.CommandRegistry;
import com.baulsupp.oksocial.commands.MainAware;
import com.baulsupp.oksocial.commands.OksocialCommand;
import com.baulsupp.oksocial.commands.ShellCommand;
import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.baulsupp.oksocial.completion.TmpCompletionVariableCache;
import com.baulsupp.oksocial.completion.UrlCompleter;
import com.baulsupp.oksocial.completion.UrlList;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.FixedTokenCredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.credentials.PreferencesCredentialsStore;
import com.baulsupp.oksocial.jjs.JavascriptApiCommand;
import com.baulsupp.oksocial.network.DnsOverride;
import com.baulsupp.oksocial.network.DnsSelector;
import com.baulsupp.oksocial.network.InterfaceSocketFactory;
import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture;
import com.baulsupp.oksocial.output.ConsoleHandler;
import com.baulsupp.oksocial.output.DownloadHandler;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.security.CertificatePin;
import com.baulsupp.oksocial.security.CertificateUtils;
import com.baulsupp.oksocial.security.ConsoleCallbackHandler;
import com.baulsupp.oksocial.security.InsecureHostnameVerifier;
import com.baulsupp.oksocial.security.InsecureTrustManager;
import com.baulsupp.oksocial.security.OpenSCUtil;
import com.baulsupp.oksocial.services.twitter.TwitterCachingInterceptor;
import com.baulsupp.oksocial.services.twitter.TwitterDeflatedResponseInterceptor;
import com.baulsupp.oksocial.util.FileContent;
import com.baulsupp.oksocial.util.InetAddressParam;
import com.baulsupp.oksocial.util.LoggingUtil;
import com.baulsupp.oksocial.util.ProtocolUtil;
import com.baulsupp.oksocial.util.UsageException;
import com.baulsupp.oksocial.util.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mcdermottroe.apple.OSXKeychainException;
import com.moczul.ok2curl.CurlInterceptor;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.IOUtils;

import static com.baulsupp.oksocial.security.CertificateUtils.trustManagerForKeyStore;
import static com.baulsupp.oksocial.security.KeystoreUtils.createKeyManager;
import static com.baulsupp.oksocial.security.KeystoreUtils.createSslSocketFactory;
import static com.baulsupp.oksocial.security.KeystoreUtils.getKeyStore;
import static com.baulsupp.oksocial.security.KeystoreUtils.keyManagerArray;
import static com.baulsupp.oksocial.util.Util.optionalStream;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Command(name = Main.NAME, description = "A curl for social apis.")
public class Main extends HelpOption implements Runnable {
  private static Logger logger = Logger.getLogger(Main.class.getName());

  static final String NAME = "oksocial";
  private static String[] commandLineArgs;

  // store active logger to avoid GC
  private Logger activeLogger;

  private static Main fromArgs(String... args) {
    Main.commandLineArgs = args;

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
  public Integer connectTimeout;

  @Option(name = "--read-timeout", description = "Maximum time allowed for reading data (seconds)")
  public Integer readTimeout;

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

  @Option(name = {"--dns"}, description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)",
      allowedValues = {"system", "ipv4", "ipv6", "ipv4only", "ipv6only"})
  public String ipmode = "system";

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
  public String urlCompletion;

  @Option(name = {"--zipkin-local"}, description = "Record zipkin traces")
  public boolean zipkinLocal;

  @Option(name = {"--zipkin-remote"}, description = "Record zipkin traces to server")
  public InetAddressParam zipkinServer;

  @Option(name = {"--zipkin-debug"}, description = "Record zipkin traces to debug")
  public boolean zipkinDebug;

  public String commandName = System.getProperty("command.name", "oksocial");

  public String completionFile = System.getenv("COMPLETION_FILE");

  @Arguments(title = "arguments", description = "Remote resource URLs")
  public List<String> arguments = new ArrayList<>();

  public ServiceInterceptor serviceInterceptor = null;

  private OkHttpClient authClient = null;
  public OkHttpClient client = null;

  public Request.Builder requestBuilder;

  public CommandRegistry commandRegistry = new CommandRegistry();

  public OutputHandler outputHandler = null;

  public CredentialsStore credentialsStore = null;

  public CompletionVariableCache completionVariableCache;

  private List<Closeable> completionList = Lists.newArrayList();

  private String versionString() {
    return Util.versionString("/oksocial-version.properties");
  }

  @Override public void run() {
    LoggingUtil.configureLogging(debug, showHttp2Frames);

    if (outputHandler == null) {
      outputHandler = buildHandler();
    }

    if (showHelpIfRequested()) {
      return;
    }

    try {
      if (version) {
        outputHandler.info(NAME + " " + versionString());
        return;
      }

      initialise();

      if (showCredentials) {
        showCredentials();
        return;
      }

      if (aliasNames) {
        printAliasNames();
        return;
      }

      if (serviceNames) {
        outputHandler.info(serviceInterceptor.names().stream().collect(joining(" ")));
        return;
      }

      if (urlCompletion != null) {
        outputHandler.info(urlCompletionList());
        return;
      }

      if (authorize) {
        authorize();
        return;
      }

      executeRequests(outputHandler);
    } catch (Exception e) {
      outputHandler.showError("unknown error", e);
    } finally {
      closeClients();
    }
  }

  private String urlCompletionList() throws Exception {
    UrlCompleter completer =
        new UrlCompleter(serviceInterceptor.services(), client, credentialsStore,
            completionVariableCache);

    Optional<String> fullCompletionUrlOpt = getFullCompletionUrl();

    if (fullCompletionUrlOpt.isPresent()) {
      String fullCompletionUrl = fullCompletionUrlOpt.get();
      UrlList urls = completer.urlList(fullCompletionUrl);

      final int strip;
      if (!fullCompletionUrl.equals(urlCompletion)) {
        strip = fullCompletionUrl.length() - urlCompletion.length();
      } else {
        strip = 0;
      }

      if (completionFile != null) {
        urls.toFile(new File(completionFile), strip, urlCompletion);
      }

      List<String> list = urls.getUrls(fullCompletionUrl);

      return list.stream()
          .map(u -> u.substring(strip))
          .collect(joining("\n"));
    } else {
      return "";
    }
  }

  private Optional<String> getFullCompletionUrl() throws Exception {
    ShellCommand command = getShellCommand();

    if (command instanceof JavascriptApiCommand) {
      ((JavascriptApiCommand) command).setMain(this);

      ArrayList<String> newArguments = Lists.newArrayList(arguments);
      newArguments.add(urlCompletion);

      List<Request> requests = command.buildRequests(client, requestBuilder, newArguments);

      if (requests.size() > 0) {
        HttpUrl newUrl = requests.get(0).url();

        // support "" -> http://api.test.com
        if (urlCompletion.isEmpty() && newUrl.encodedPath().equals("/")) {
          urlCompletion = "/";
        }

        String newUrlCompletion = newUrl.toString();

        if (newUrlCompletion.endsWith(urlCompletion)) {
          return Optional.of(newUrlCompletion);
        }
      }
    } else if (UrlCompleter.isPossibleAddress(urlCompletion)) {
      return Optional.of(urlCompletion);
    }

    return Optional.empty();
  }

  public void initialise() throws Exception {
    if (outputHandler == null) {
      outputHandler = buildHandler();
    }

    if (credentialsStore == null) {
      credentialsStore = createCredentialsStore();
    }

    authClient = build(createClientBuilder());

    serviceInterceptor = new ServiceInterceptor(authClient, credentialsStore);

    OkHttpClient.Builder clientBuilder = authClient.newBuilder();
    clientBuilder.networkInterceptors().add(0, serviceInterceptor);

    ZipkinHandler zipkinHandler;
    if (zipkinDebug) {
      zipkinHandler = BaseZipkinHandler.logging();
    } else if (zipkinLocal) {
      zipkinHandler = ServerZipkinHandler.localhost(outputHandler);
    } else if (zipkinServer != null) {
      zipkinHandler = ServerZipkinHandler.instance(zipkinServer.address, outputHandler);
    } else {
      zipkinHandler = NullZipkinHandler.instance();
    }

    zipkinHandler.configureClient(Arrays.asList(commandLineArgs), clientBuilder);

    closeOnComplete(zipkinHandler);

    client = build(clientBuilder);

    requestBuilder = createRequestBuilder();

    if (completionVariableCache == null) {
      completionVariableCache = new TmpCompletionVariableCache();
    }
  }

  private void closeOnComplete(Closeable c) {
    completionList.add(c);
  }

  public OkHttpClient getClient() {
    return client;
  }

  private CredentialsStore createCredentialsStore() throws OSXKeychainException {
    if (token != null && !authorize) {
      return new FixedTokenCredentialsStore(token);
    } else if (Util.isOSX()) {
      return new OSXCredentialsStore(Optional.ofNullable(tokenSet));
    } else {
      return new PreferencesCredentialsStore(Optional.ofNullable(tokenSet));
    }
  }

  private void closeClients() {
    for (Closeable closeable : completionList) {
      IOUtils.closeQuietly(closeable);
    }
  }

  private void showCredentials() throws Exception {
    Iterable<AuthInterceptor<?>> services = serviceInterceptor.services();

    boolean full = !arguments.isEmpty();

    if (!arguments.isEmpty()) {
      services = arguments.stream().flatMap(a ->
          optionalStream(findAuthInterceptor(a))).collect(
          toList());
    }

    PrintCredentials printCredentials =
        new PrintCredentials(client, credentialsStore);
    printCredentials.printKnownCredentials(createRequestBuilder(), services, full);
  }

  private OutputHandler buildHandler() {
    if (outputDirectory != null) {
      return new DownloadHandler(outputDirectory);
    } else if (rawOutput) {
      return new DownloadHandler(new File("-"));
    } else {
      return ConsoleHandler.instance();
    }
  }

  private void executeRequests(OutputHandler outputHandler) throws Exception {
    ShellCommand command = getShellCommand();

    if (command instanceof MainAware) {
      ((MainAware) command).setMain(this);
    }

    List<Request> requests = command.buildRequests(client, requestBuilder, arguments);

    if (requests.isEmpty()) {
      throw new UsageException("no urls specified");
    }

    List<Future<Response>> responseFutures = enqueueRequests(requests, client);
    processResponses(outputHandler, responseFutures);
  }

  private void processResponses(OutputHandler outputHandler, List<Future<Response>> responseFutures)
      throws IOException, InterruptedException {
    boolean failed = false;
    for (Future<Response> responseFuture : responseFutures) {
      if (failed) {
        responseFuture.cancel(true);
      } else {
        try (Response response = responseFuture.get()) {
          outputHandler.showOutput(response, showHeaders);
        } catch (ExecutionException ee) {
          // TODO allow setting failure/cancel strategy
          outputHandler.showError("request failed", ee.getCause());
          failed = true;
        }
      }
    }
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

  private OkHttpClient build(OkHttpClient.Builder clientBuilder) {
    OkHttpClient client = clientBuilder.build();

    closeOnComplete(() -> {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    });

    return client;
  }

  private ShellCommand getShellCommand() throws Exception {
    return commandRegistry.getCommandByName(commandName).orElse(new OksocialCommand());
  }

  private void printAliasNames() {
    Set<String> names = Sets.newTreeSet(commandRegistry.names());

    names.forEach(outputHandler::info);
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

    List<String> authArguments = Lists.newArrayList(arguments);

    Optional<AuthInterceptor<?>> auth =
        command.authenticator().flatMap(authName -> interceptorByName(authName));

    if (!auth.isPresent() && !authArguments.isEmpty()) {
      String name = authArguments.remove(0);

      auth = findAuthInterceptor(name);
    }

    if (!auth.isPresent()) {
      throw new UsageException(
          "unable to find authenticator. Specify name from " + serviceInterceptor.names()
              .stream()
              .collect(joining(", ")));
    }

    if (token != null) {
      storeCredentials(auth.get());
    } else {
      authRequest(auth.get(), authArguments);
    }
  }

  public Optional<AuthInterceptor<?>> interceptorByName(String authName) {
    return serviceInterceptor.getByName(authName);
  }

  private Optional<AuthInterceptor<?>> findAuthInterceptor(String name) {
    Optional<AuthInterceptor<?>> auth = interceptorByName(name);

    if (!auth.isPresent()) {
      auth = serviceInterceptor.getByUrl(name);
    }

    return auth;
  }

  private <T> void storeCredentials(AuthInterceptor<T> auth) {
    T credentials = auth.serviceDefinition().parseCredentialsString(token);
    credentialsStore.storeCredentials(credentials, auth.serviceDefinition());
  }

  private <T> void authRequest(AuthInterceptor<T> auth, List<String> authArguments)
      throws Exception {
    OkHttpClient.Builder b = client.newBuilder();
    b.networkInterceptors().removeIf(ServiceInterceptor.class::isInstance);
    OkHttpClient authClient = build(b);

    T credentials = auth.authorize(authClient, outputHandler, authArguments);

    credentialsStore.storeCredentials(credentials, auth.serviceDefinition());

    Secrets.instance().saveIfNeeded();

    // TODO validate credentials
  }

  public OkHttpClient.Builder createClientBuilder() throws Exception {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.followSslRedirects(followRedirects);
    if (connectTimeout != null) {
      builder.connectTimeout(connectTimeout, SECONDS);
    }
    if (readTimeout != null) {
      builder.readTimeout(readTimeout, SECONDS);
    }

    Dns dns = DnsSelector.byName(ipmode);
    if (resolve != null) {
      dns = DnsOverride.build(dns, resolve);
    }
    builder.dns(dns);

    if (networkInterface != null) {
      builder.socketFactory(InterfaceSocketFactory.byName(networkInterface));
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
      builder.networkInterceptors().add(new HttpLoggingInterceptor(s -> logger.info(s)));
    }

    if (socksProxy != null) {
      builder.proxy(new Proxy(Proxy.Type.SOCKS, socksProxy.address));
    } else if (proxy != null) {
      builder.proxy(new Proxy(Proxy.Type.HTTP, proxy.address));
    }

    if (protocols != null) {
      builder.protocols(ProtocolUtil.parseProtocolList(protocols));
    }

    return builder;
  }

  private void configureTls(OkHttpClient.Builder builder) throws Exception {
    ConsoleCallbackHandler callbackHandler = new ConsoleCallbackHandler();

    // possibly null
    KeyStore keystore = null;

    if (keystoreFile != null) {
      keystore = getKeyStore(keystoreFile);
    }

    List<KeyManager> keyManagers = Lists.newArrayList();

    if (opensc != null) {
      keyManagers.addAll(asList(OpenSCUtil.getKeyManagers(callbackHandler, opensc)));
    } else if (clientAuth) {
      if (keystore == null) {
        throw new UsageException("--clientauth specified without --keystore");
      }

      keyManagers.add(createKeyManager(keystore, callbackHandler));
    }

    X509TrustManager trustManager = null;
    if (allowInsecure) {
      trustManager = new InsecureTrustManager();
      builder.hostnameVerifier(new InsecureHostnameVerifier());
    } else {
      List<X509TrustManager> trustManagers = Lists.newArrayList();

      if (keystore != null) {
        trustManagers.add(trustManagerForKeyStore(keystore));
      }

      if (!serverCerts.isEmpty()) {
        trustManagers.add(CertificateUtils.load(serverCerts));
      }

      trustManager = CertificateUtils.combineTrustManagers(trustManagers);
    }

    builder.sslSocketFactory(createSslSocketFactory(keyManagerArray(keyManagers), trustManager),
        trustManager);

    if (certificatePins != null) {
      builder.certificatePinner(CertificatePin.buildFromCommandLine(certificatePins));
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

  private RequestBody getRequestBody() throws IOException {
    if (data == null) {
      return null;
    }

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

    return RequestBody.create(MediaType.parse(mimeType), FileContent.readParamBytes(data));
  }

  public Request.Builder createRequestBuilder() throws IOException {
    Request.Builder requestBuilder = new Request.Builder();

    requestBuilder.method(getRequestMethod(), getRequestBody());

    if (headers != null) {
      for (String header : headers) {
        String[] parts = header.split(":", 2);
        requestBuilder.header(parts[0], parts[1]);
      }
    }
    if (referer != null) {
      requestBuilder.header("Referer", referer);
    }
    requestBuilder.header("User-Agent", userAgent);

    return requestBuilder;
  }
}
