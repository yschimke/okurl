package com.baulsupp.oksocial

import brave.Tracing
import brave.http.HttpTracing
import brave.internal.Platform
import brave.propagation.TraceContext
import brave.sampler.Sampler
import com.baulsupp.oksocial.Main.Companion.NAME
import com.baulsupp.oksocial.apidocs.ServiceApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.Authorisation
import com.baulsupp.oksocial.authenticator.PrintCredentials
import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.commands.CommandRegistry
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.OksocialCommand
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.completion.ArgumentCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.TmpCompletionVariableCache
import com.baulsupp.oksocial.completion.UrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.FixedTokenCredentialsStore
import com.baulsupp.oksocial.credentials.OSXCredentialsStore
import com.baulsupp.oksocial.credentials.PreferencesCredentialsStore
import com.baulsupp.oksocial.jjs.OkApiCommand
import com.baulsupp.oksocial.kotlin.await
import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.DnsOverride
import com.baulsupp.oksocial.network.DnsSelector
import com.baulsupp.oksocial.network.GoogleDns
import com.baulsupp.oksocial.network.InterfaceSocketFactory
import com.baulsupp.oksocial.network.NettyDns
import com.baulsupp.oksocial.okhttp.FailedResponse
import com.baulsupp.oksocial.okhttp.OkHttpResponseExtractor
import com.baulsupp.oksocial.okhttp.PotentialResponse
import com.baulsupp.oksocial.okhttp.SuccessfulResponse
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.DownloadHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.PlatformUtil
import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.security.CertificatePin
import com.baulsupp.oksocial.security.CertificateUtils
import com.baulsupp.oksocial.security.ConsoleCallbackHandler
import com.baulsupp.oksocial.security.InsecureHostnameVerifier
import com.baulsupp.oksocial.security.InsecureTrustManager
import com.baulsupp.oksocial.security.KeystoreUtils
import com.baulsupp.oksocial.security.OpenSCUtil
import com.baulsupp.oksocial.services.twitter.TwitterCachingInterceptor
import com.baulsupp.oksocial.services.twitter.TwitterDeflatedResponseInterceptor
import com.baulsupp.oksocial.tracing.UriTransportRegistry
import com.baulsupp.oksocial.tracing.ZipkinConfig
import com.baulsupp.oksocial.tracing.ZipkinTracingInterceptor
import com.baulsupp.oksocial.tracing.ZipkinTracingListener
import com.baulsupp.oksocial.util.FileContent
import com.baulsupp.oksocial.util.HeaderUtil
import com.baulsupp.oksocial.util.LoggingUtil
import com.baulsupp.oksocial.util.ProtocolUtil
import com.mcdermottroe.apple.OSXKeychainException
import com.moczul.ok2curl.CurlInterceptor
import io.airlift.airline.Command
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.StatusLine
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.IOUtils
import zipkin.Span
import zipkin.reporter.Reporter
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.net.Proxy
import java.net.SocketException
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.SocketFactory
import javax.net.ssl.KeyManager
import javax.net.ssl.X509TrustManager

@Command(name = NAME, description = "A curl for social apis.")
class Main : CommandLineClient() {
  private val logger = Logger.getLogger(Main::class.java.name)

  @Option(name = ["-X", "--request"], description = "Specify request command to use")
  var method: String? = null

  @Option(name = ["-d", "--data"], description = "HTTP POST data")
  var data: String? = null

  @Option(name = ["-H", "--header"], description = "Custom header to pass to server")
  var headers: java.util.List<String>? = null

  @Option(name = ["--no-follow"], description = "Follow redirects")
  var dontFollowRedirects = false

  @Option(name = ["-e", "--referer"], description = "Referer URL")
  var referer: String? = null

  @Option(name = ["-o", "--output"], description = "Output file/directory")
  var outputDirectory: File? = null

  @Option(name = ["--authorize"], description = "Authorize API")
  var authorize: Boolean = false

  @Option(name = ["--renew"], description = "Renew API Authorization")
  var renew: Boolean = false

  @Option(name = ["--token"], description = "Use existing Token for authorization")
  var token: String? = null

  @Option(name = ["--curl"], description = "Show curl commands")
  var curl = false

  @Option(name = ["--show-credentials"], description = "Show Credentials")
  var showCredentials = false

  @Option(name = ["--alias-names"], description = "Show Alias Names")
  var aliasNames = false

  @Option(name = ["-r", "--raw"], description = "Raw Output")
  var rawOutput = false

  @Option(name = ["--serviceNames"], description = "Service Names")
  var serviceNames = false

  @Option(name = ["--urlCompletion"], description = "URL Completion")
  var urlComplete: Boolean = false

  @Option(name = ["--apidoc"], description = "API Documentation")
  var apiDoc: Boolean = false

  var commandName = System.getProperty("command.name", "oksocial")!!

  var completionFile: String? = System.getenv("COMPLETION_FILE")

  var commandRegistry = CommandRegistry()

  var completionVariableCache: CompletionVariableCache? = null

  fun run(): Int {
    if (sslDebug) {
      System.setProperty("javax.net.debug", "ssl,handshake")
    }

    LoggingUtil.configureLogging(debug, showHttp2Frames)

    if (outputHandler == null) {
      outputHandler = buildHandler()
    }

    if (showHelpIfRequested()) {
      return 0
    }

    try {
      if (version) {
        outputHandler!!.info(NAME + " " + versionString())
        return 0
      }

      initialise()

      if (showCredentials) {
        runBlocking {
          PrintCredentials(client!!, credentialsStore!!, outputHandler!!, serviceInterceptor!!).showCredentials(arguments)
        }
        return 0
      }

      if (aliasNames) {
        printAliasNames()
        return 0
      }

      if (serviceNames) {
        outputHandler!!.info(serviceInterceptor!!.names().joinToString(" "))
        return 0
      }

      if (urlComplete) {
        outputHandler!!.info(urlCompletionList())
        return 0
      }

      if (apiDoc) {
        runBlocking { showApiDocs() }
        return 0
      }

      if (authorize) {
        runBlocking { authorize() }
        return 0
      }

      if (renew) {
        runBlocking { renew() }
        return 0
      }

      return runBlocking { executeRequests(outputHandler!!) }
    } catch (e: UsageException) {
      outputHandler!!.showError("error: " + e.message, null)
      return -1
    } catch (e: Exception) {
      outputHandler!!.showError("unknown error", e)
      return -2
    } finally {
      closeClients()
    }
  }

  suspend fun showApiDocs() {
    val docs = ServiceApiDocPresenter(serviceInterceptor!!)

    getFullCompletionUrl()?.let { u ->
      docs.explainApi(u, outputHandler!!, client!!)
    }
  }

  // TODO refactor this mess out of Main
  private fun urlCompletionList(): String {
    val command = getShellCommand()

    val commandCompletor = command.completer()
    if (commandCompletor != null) {
      val urls = runBlocking {
        commandCompletion(commandCompletor, arguments)
      }

      val prefix = arguments[arguments.size - 1]

      if (completionFile != null) {
        urls.toFile(File(completionFile), 0, prefix)
      }

      return urls.getUrls(prefix).joinToString("\n")
    }

    val completer = UrlCompleter(serviceInterceptor!!.services(), client!!, credentialsStore!!,
            completionVariableCache!!)

    val fullCompletionUrl = getFullCompletionUrl()

    // reload hack (in case changed for "" case)
    val originalCompletionUrl = arguments[arguments.size - 1]

    if (fullCompletionUrl != null) {
      val urls = runBlocking {
        completer.urlList(fullCompletionUrl)
      }

      val strip: Int = if (fullCompletionUrl != originalCompletionUrl) {
        fullCompletionUrl.length - originalCompletionUrl.length
      } else {
        0
      }

      if (completionFile != null) {
        urls.toFile(File(completionFile), strip, originalCompletionUrl)
      }

      return urls.getUrls(fullCompletionUrl).joinToString("\n") { it.substring(strip) }
    } else {
      return ""
    }
  }

  fun applyRequestFields(request: Request): Request {
    val requestBuilder = request.newBuilder()

    val headerMap = HeaderUtil.headerMap(headers?.toList()).toMutableMap()

    requestBuilder.method(getRequestMethod(), getRequestBody(headerMap))

    if (headers != null) {
      headerMap.forEach { k, v -> requestBuilder.header(k, v) }
    }
    if (referer != null) {
      requestBuilder.header("Referer", referer!!)
    }
    requestBuilder.header("User-Agent", userAgent)

    return requestBuilder.build()
  }

  suspend fun commandCompletion(urlCompleter: ArgumentCompleter, arguments: List<String>): UrlList {
    return urlCompleter.urlList(arguments[arguments.size - 1])
  }

  /*
   * The last url in arguments which should be used for completion or apidoc requests.
   * In the case of javascript command expansion, it is expanded first before
   * being returned.
   *
   * n.b. arguments may be modified by this call.
   */
  private fun getFullCompletionUrl(): String? {
    if (arguments.isEmpty()) {
      return null
    }

    var urlToComplete = arguments[arguments.size - 1]

    val command = getShellCommand()

    if (command is OkApiCommand) {
      val requests = command.buildRequests(client!!, arguments)

      if (requests.isNotEmpty()) {
        val newUrl = requests[0].url()

        // support "" -> http://api.test.com
        if (urlToComplete.isEmpty() && newUrl.encodedPath() == "/") {
          urlToComplete = "/"
          arguments.removeAt(arguments.size - 1)
          arguments.add(urlToComplete)
        }

        val newUrlCompletion = newUrl.toString()

        if (newUrlCompletion.endsWith(urlToComplete)) {
          return newUrlCompletion
        }
      }
    } else if (UrlCompleter.isPossibleAddress(urlToComplete)) {
      return urlToComplete
    }

    return null
  }

  @Throws(Exception::class)
  fun initialise() {
    if (outputHandler == null) {
      outputHandler = buildHandler()
    }

    if (credentialsStore == null) {
      credentialsStore = createCredentialsStore()
    }

    closeables.add(Closeable {
      if (client != null) {
        client!!.dispatcher().executorService().shutdown()
        client!!.connectionPool().evictAll()
      }
    })

    val clientBuilder = createClientBuilder()

    if (user != null) {
      val userParts = user!!.split(":".toRegex(), 2).toTypedArray()
      if (userParts.size < 2) {
        throw UsageException("--user should have user:password")
      }
      val credential = Credentials.basic(userParts[0], userParts[1])

      clientBuilder.authenticator({ _, response ->
        logger.fine("Challenges: " + response.challenges())

        // authenticate once
        if (response.request().header("Authorization") != null) {
          null
        } else {
          response.request().newBuilder()
                  .header("Authorization", credential)
                  .build()
        }
      })
    }

    val dispatcher = Dispatcher()
    dispatcher.maxRequests = maxRequests
    dispatcher.maxRequestsPerHost = maxRequests
    clientBuilder.dispatcher(dispatcher)

    val authClient = clientBuilder.build()
    serviceInterceptor = ServiceInterceptor(authClient, credentialsStore!!)

    authorisation = Authorisation(serviceInterceptor!!, credentialsStore!!, authClient, outputHandler!!)

    clientBuilder.networkInterceptors().add(0, serviceInterceptor)

    if (zipkin || zipkinTrace) {
      applyZipkin(clientBuilder)
    }

    client = clientBuilder.build()

    if (completionVariableCache == null) {
      completionVariableCache = TmpCompletionVariableCache()
    }
  }

  fun createClientBuilder(): OkHttpClient.Builder {
    val builder = OkHttpClient.Builder()
    builder.followSslRedirects(!dontFollowRedirects)
    builder.followRedirects(!dontFollowRedirects)
    if (connectTimeout != null) {
      builder.connectTimeout(connectTimeout!!.toLong(), TimeUnit.SECONDS)
    }
    if (readTimeout != null) {
      builder.readTimeout(readTimeout!!.toLong(), TimeUnit.SECONDS)
    }

    builder.dns(buildDns())

    if (networkInterface != null) {
      builder.socketFactory(getSocketFactory())
    }

    configureTls(builder)

    if (cacheDirectory != null) {
      builder.cache(Cache(cacheDirectory!!, (64 * 1024 * 1024).toLong()))
    }

    // TODO move behind AuthInterceptor API
    builder.addNetworkInterceptor(TwitterCachingInterceptor())
    builder.addNetworkInterceptor(TwitterDeflatedResponseInterceptor())

    if (curl) {
      builder.addNetworkInterceptor(CurlInterceptor(System.err::println))
    }

    if (debug) {
      val loggingInterceptor = HttpLoggingInterceptor(logger::info)
      loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
      builder.networkInterceptors().add(loggingInterceptor)
    }

    if (socksProxy != null) {
      builder.proxy(Proxy(Proxy.Type.SOCKS, socksProxy!!.address))
    } else if (proxy != null) {
      builder.proxy(Proxy(Proxy.Type.HTTP, proxy!!.address))
    }

    if (protocols != null) {
      builder.protocols(ProtocolUtil.parseProtocolList(protocols!!))
    }

    return builder
  }

  @Throws(IOException::class)
  private fun applyZipkin(clientBuilder: OkHttpClient.Builder) {
    val config = ZipkinConfig.load()
    val zipkinSenderUri = config.zipkinSenderUri()
    val reporter: Reporter<Span>

    reporter = if (zipkinSenderUri != null) {
      UriTransportRegistry.forUri(zipkinSenderUri)
    } else {
      Platform.get()
    }

    val tracing = Tracing.newBuilder()
            .localServiceName("oksocial")
            .reporter(reporter)
            .sampler(Sampler.ALWAYS_SAMPLE)
            .build()

    val httpTracing = HttpTracing.create(tracing)

    val tracer = tracing.tracer()

    val opener: Consumer<TraceContext> = Consumer { tc ->
      closeables.add(Closeable {
        val link = config.openFunction().invoke(tc)

        if (link != null) {
          openLink(link)
        }
      })
    }

    clientBuilder.eventListenerFactory { call -> ZipkinTracingListener(call, tracer, httpTracing, opener, zipkinTrace) }

    clientBuilder.addNetworkInterceptor(ZipkinTracingInterceptor(tracing))

    closeables.add(Closeable {
      tracing.close()
      if (reporter is Flushable) {
        (reporter as Flushable).flush()
      }
      if (reporter is Closeable) {
        (reporter as Closeable).close()
      }
    })
  }

  private fun openLink(link: String) {
    try {
      outputHandler!!.openLink(link)
    } catch (e: IOException) {
      outputHandler!!.showError("Can't open link", e)
    }

  }

  @Throws(OSXKeychainException::class)
  private fun createCredentialsStore(): CredentialsStore {
    if (token != null && !authorize) {
      return FixedTokenCredentialsStore(token!!)
    }

    return if (PlatformUtil.isOSX) {
      OSXCredentialsStore(tokenSet)
    } else {
      PreferencesCredentialsStore(tokenSet)
    }
  }

  private fun closeClients() {
    for (c in closeables) {
      IOUtils.closeQuietly(c)
    }
  }

  private fun buildHandler(): OutputHandler<Response> {
    val responseExtractor = OkHttpResponseExtractor()

    return when {
      outputDirectory != null -> DownloadHandler(responseExtractor, outputDirectory!!)
      rawOutput -> DownloadHandler(responseExtractor, File("-"))
      else -> ConsoleHandler.instance(responseExtractor) as OutputHandler<Response>
    }
  }

  @Throws(Exception::class)
  private suspend fun executeRequests(outputHandler: OutputHandler<Response>): Int {
    val command = getShellCommand()

    val requests = command.buildRequests(client!!, arguments).map(this::applyRequestFields)

    if (!command.handlesRequests()) {
      if (requests.isEmpty()) {
        throw UsageException("no urls specified")
      }

      val responses = enqueueRequests(requests, client!!)
      val failed = processResponses(outputHandler, responses)
      return if (failed) -5 else 0
    }

    return 0
  }

  @Throws(IOException::class, InterruptedException::class)
  private fun processResponses(outputHandler: OutputHandler<Response>,
                               responses: List<PotentialResponse>): Boolean {
    var failed = false
    for (response in responses) {
      when (response) {
        is SuccessfulResponse -> {
          showOutput(outputHandler, response.response)
          response.response.close()
        }
        is FailedResponse -> {
          outputHandler.showError("request failed", response.exception)
          failed = true
        }
      }
    }
    return failed
  }

  @Throws(IOException::class)
  private fun showOutput(outputHandler: OutputHandler<Response>, response: Response) {
    if (showHeaders) {
      outputHandler.info(StatusLine.get(response).toString())
      val headers = response.headers()
      var i = 0
      val size = headers.size()
      while (i < size) {
        outputHandler.info(headers.name(i) + ": " + headers.value(i))
        i++
      }
      outputHandler.info("")
    } else if (!response.isSuccessful) {
      outputHandler.showError(StatusLine.get(response).toString(), null)
    }

    outputHandler.showOutput(response)
  }

  private suspend fun enqueueRequests(requests: List<Request>, client: OkHttpClient): List<PotentialResponse> {
    val responses = mutableListOf<PotentialResponse>()

    for (request in requests) {
      logger.log(Level.FINE, "url " + request.url())

      if (requests.size > 1 && !debug) {
        System.err.println(request.url())
      }

      responses.add(makeRequest(client, request))
    }
    return responses
  }

  private fun getShellCommand(): ShellCommand {
    var shellCommand = commandRegistry.getCommandByName(commandName)

    if (shellCommand == null) {
      shellCommand = OksocialCommand()
    }

    if (shellCommand is MainAware) {
      (shellCommand as MainAware).setMain(this)
    }

    return shellCommand
  }

  private fun printAliasNames() {
    val names = commandRegistry.names().sorted()

    names.forEach({ outputHandler!!.info(it) })
  }

  private suspend fun makeRequest(client: OkHttpClient, request: Request): PotentialResponse {
    logger.log(Level.FINE, "Request " + request)

    return try {
      SuccessfulResponse(client.newCall(request).await())
    } catch (ioe: IOException) {
      FailedResponse(ioe)
    }
  }

  suspend fun authorize() {
    authorisation!!.authorize(findAuthInterceptor(), token, arguments)
  }

  suspend fun renew() {
    authorisation!!.renew(findAuthInterceptor())
  }

  @Throws(Exception::class)
  private fun findAuthInterceptor(): AuthInterceptor<*>? {
    val command = getShellCommand()

    val authenticator = command.authenticator()
    var auth: AuthInterceptor<*>? = null

    if (authenticator != null) {
      auth = serviceInterceptor!!.getByName(authenticator)
    }

    if (auth == null && !arguments.isEmpty()) {
      val name = arguments.removeAt(0)

      auth = serviceInterceptor!!.findAuthInterceptor(name)
    }
    return auth
  }

  private fun buildDns(): Dns {
    var dns: Dns
    dns = when {
      dnsMode === DnsMode.NETTY -> NettyDns.byName(ipMode, createEventLoopGroup(), this.dnsServers!!)
      dnsMode === DnsMode.DNSGOOGLE -> DnsSelector(ipMode,
              GoogleDns.fromHosts({ this@Main.client!! }, ipMode, "216.58.216.142", "216.239.34.10",
                      "2607:f8b0:400a:809::200e"))
      else -> {
        if (dnsServers != null) {
          throw UsageException("unable to set dns servers with java DNS")
        }

        DnsSelector(ipMode, Dns.SYSTEM)
      }
    }
    if (resolve != null) {
      dns = DnsOverride.build(dns, resolve!!)
    }
    return dns
  }

  private fun createEventLoopGroup(): NioEventLoopGroup {
    if (eventLoopGroup == null) {
      val threadFactory = DefaultThreadFactory("netty", true)
      eventLoopGroup = NioEventLoopGroup(5, threadFactory)

      closeables.add(Closeable { eventLoopGroup!!.shutdownGracefully(0, 0, TimeUnit.SECONDS) })
    }

    return eventLoopGroup!!
  }

  @Throws(SocketException::class)
  private fun getSocketFactory(): SocketFactory =
          InterfaceSocketFactory.byName(networkInterface!!) ?: throw UsageException("networkInterface '$networkInterface' not found")

  @Throws(Exception::class)
  private fun configureTls(builder: OkHttpClient.Builder) {
    val callbackHandler = ConsoleCallbackHandler()

    // possibly null
    var keystore: KeyStore? = null

    if (keystoreFile != null) {
      keystore = KeystoreUtils.getKeyStore(keystoreFile)
    }

    val keyManagers = mutableListOf<KeyManager>()

    if (opensc != null) {
      keyManagers.addAll(OpenSCUtil.getKeyManagers(callbackHandler, opensc!!).asIterable())
    } else if (clientAuth) {
      if (keystore == null) {
        throw UsageException("--clientauth specified without --keystore")
      }

      keyManagers.add(KeystoreUtils.createKeyManager(keystore, callbackHandler))
    }

    val trustManager: X509TrustManager
    if (allowInsecure) {
      trustManager = InsecureTrustManager()
      builder.hostnameVerifier(InsecureHostnameVerifier())
    } else {
      val trustManagers = mutableListOf<X509TrustManager>()

      if (keystore != null) {
        trustManagers.add(CertificateUtils.trustManagerForKeyStore(keystore))
      }

      if (serverCerts != null) {
        trustManagers.add(CertificateUtils.load(serverCerts!!.toList()))
      }

      trustManager = CertificateUtils.combineTrustManagers(trustManagers)
    }

    builder.sslSocketFactory(KeystoreUtils.createSslSocketFactory(KeystoreUtils.keyManagerArray(keyManagers), trustManager),
            trustManager)

    if (certificatePins != null) {
      builder.certificatePinner(CertificatePin.buildFromCommandLine(certificatePins!!.toList()))
    }
  }

  private fun getRequestMethod(): String {
    if (method != null) {
      return method!!
    }
    return if (data != null) {
      "POST"
    } else "GET"
  }

  private fun getRequestBody(headerMap: MutableMap<String, String>): RequestBody? {
    if (data == null) {
      return null
    }

    val mimeType = headerMap.keys
            .firstOrNull { "Content-Type".equals(it, ignoreCase = true) }
            ?.let { headerMap.remove(it)!! }
            ?: "application/x-www-form-urlencoded"

    return try {
      RequestBody.create(MediaType.parse(mimeType), FileContent.readParamBytes(data!!))
    } catch (e: IOException) {
      throw UsageException(e.message!!)
    }

  }

  companion object {
    const val NAME = "oksocial"

    private fun fromArgs(vararg args: String): Main {
      return SingleCommand.singleCommand(Main::class.java).parse(*args)
    }

    @JvmStatic
    fun main(vararg args: String) {
      val result = fromArgs(*args).run()
      System.exit(result)
    }
  }
}