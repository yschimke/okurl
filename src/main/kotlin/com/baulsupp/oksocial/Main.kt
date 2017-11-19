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
import com.baulsupp.oksocial.jjs.JavascriptApiCommand
import com.baulsupp.oksocial.kotlin.await
import com.baulsupp.oksocial.location.BestLocation
import com.baulsupp.oksocial.location.LocationSource
import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.DnsOverride
import com.baulsupp.oksocial.network.DnsSelector
import com.baulsupp.oksocial.network.GoogleDns
import com.baulsupp.oksocial.network.IPvMode
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
import com.baulsupp.oksocial.util.InetAddressParam
import com.baulsupp.oksocial.util.LoggingUtil
import com.baulsupp.oksocial.util.ProtocolUtil
import com.google.common.collect.Lists
import com.mcdermottroe.apple.OSXKeychainException
import com.moczul.ok2curl.CurlInterceptor
import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.HelpOption
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
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.SocketFactory
import javax.net.ssl.KeyManager
import javax.net.ssl.X509TrustManager

@Command(name = NAME, description = "A curl for social apis.")
class Main : HelpOption() {
  private val logger = Logger.getLogger(Main::class.java.name)

  @Option(name = arrayOf("-X", "--request"), description = "Specify request command to use")
  var method: String? = null

  @Option(name = arrayOf("-d", "--data"), description = "HTTP POST data")
  var data: String? = null

  @Option(name = arrayOf("-H", "--header"), description = "Custom header to pass to server")
  var headers: java.util.List<String>? = null

  @Option(name = arrayOf("-A", "--user-agent"), description = "User-Agent to send to server")
  var userAgent = NAME + "/" + versionString()

  @Option(name = arrayOf("--connect-timeout"), description = "Maximum time allowed for connection (seconds)")
  var connectTimeout: Int? = null

  @Option(name = arrayOf("--read-timeout"), description = "Maximum time allowed for reading data (seconds)")
  var readTimeout: Int? = null

  @Option(name = arrayOf("--no-follow"), description = "Follow redirects")
  var dontFollowRedirects = false

  @Option(name = arrayOf("-k", "--insecure"), description = "Allow connections to SSL sites without certs")
  var allowInsecure = false

  @Option(name = arrayOf("-i", "--include"), description = "Include protocol headers in the output")
  var showHeaders = false

  @Option(name = arrayOf("--frames"), description = "Log HTTP/2 frames to STDERR")
  var showHttp2Frames = false

  @Option(name = arrayOf("--debug"), description = "Debug")
  var debug = false

  @Option(name = arrayOf("-e", "--referer"), description = "Referer URL")
  var referer: String? = null

  @Option(name = arrayOf("-V", "--version"), description = "Show version number and quit")
  var version = false

  @Option(name = arrayOf("--cache"), description = "Cache directory")
  var cacheDirectory: File? = null

  @Option(name = arrayOf("--protocols"), description = "Protocols")
  var protocols: String? = null

  @Option(name = arrayOf("-o", "--output"), description = "Output file/directory")
  var outputDirectory: File? = null

  @Option(name = arrayOf("--authorize"), description = "Authorize API")
  var authorize: Boolean = false

  @Option(name = arrayOf("--renew"), description = "Renew API Authorization")
  var renew: Boolean = false

  @Option(name = arrayOf("--token"), description = "Use existing Token for authorization")
  var token: String? = null

  @Option(name = arrayOf("--curl"), description = "Show curl commands")
  var curl = false

  @Option(name = arrayOf("--zipkin", "-z"), description = "Activate Zipkin Tracing")
  var zipkin = false

  @Option(name = arrayOf("--zipkinTrace"), description = "Activate Detailed Zipkin Tracing")
  var zipkinTrace = false

  @Option(name = arrayOf("--ip"), description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)", allowedValues = arrayOf("system", "ipv4", "ipv6", "ipv4only", "ipv6only"))
  var ipMode = IPvMode.SYSTEM

  @Option(name = arrayOf("--dns"), description = "DNS (netty, java)", allowedValues = arrayOf("java", "netty"))
  var dnsMode = DnsMode.JAVA

  @Option(name = arrayOf("--dnsServers"), description = "Specific DNS Servers (csv, google)")
  var dnsServers: String? = null

  @Option(name = arrayOf("--resolve"), description = "DNS Overrides (HOST:TARGET)")
  var resolve: List<String>? = null

  @Option(name = arrayOf("--certificatePin"), description = "Specific Local Network Interface")
  var certificatePins: java.util.List<CertificatePin>? = null

  @Option(name = arrayOf("--networkInterface"), description = "Specific Local Network Interface")
  var networkInterface: String? = null

  @Option(name = arrayOf("--clientauth"), description = "Use Client Authentication (from keystore)")
  var clientAuth = false

  @Option(name = arrayOf("--keystore"), description = "Keystore")
  var keystoreFile: File? = null

  @Option(name = arrayOf("--cert"), description = "Use given server cert (Root CA)")
  var serverCerts: java.util.List<File>? = null

  @Option(name = arrayOf("--opensc"), description = "Send OpenSC Client Certificate (slot)")
  var opensc: Int? = null

  @Option(name = arrayOf("--socks"), description = "Use SOCKS proxy")
  var socksProxy: InetAddressParam? = null

  @Option(name = arrayOf("--proxy"), description = "Use HTTP proxy")
  var proxy: InetAddressParam? = null

  @Option(name = arrayOf("--show-credentials"), description = "Show Credentials")
  var showCredentials = false

  @Option(name = arrayOf("--alias-names"), description = "Show Alias Names")
  var aliasNames = false

  @Option(name = arrayOf("-r", "--raw"), description = "Raw Output")
  var rawOutput = false

  @Option(name = arrayOf("-s", "--set"), description = "Token Set e.g. work")
  var tokenSet: String? = null

  @Option(name = arrayOf("--serviceNames"), description = "Service Names")
  var serviceNames = false

  @Option(name = arrayOf("--urlCompletion"), description = "URL Completion")
  var urlComplete: Boolean = false

  @Option(name = arrayOf("--apidoc"), description = "API Documentation")
  var apiDoc: Boolean = false

  @Option(name = arrayOf("--ssldebug"), description = "SSL Debug")
  var sslDebug: Boolean = false

  @Option(name = arrayOf("--user"), description = "user:password for basic auth")
  var user: String? = null

  @Option(name = arrayOf("--maxrequests"), description = "Concurrency Level")
  private val maxRequests = 16

  var commandName = System.getProperty("command.name", "oksocial")!!

  var completionFile: String? = System.getenv("COMPLETION_FILE")

  @Arguments(title = "arguments", description = "Remote resource URLs")
  var arguments: MutableList<String> = ArrayList()

  var serviceInterceptor: ServiceInterceptor? = null

  private var authorisation: Authorisation? = null

  var client: OkHttpClient? = null

  var requestBuilder: Request.Builder? = null

  var commandRegistry = CommandRegistry()

  var outputHandler: OutputHandler<Response>? = null

  var credentialsStore: CredentialsStore? = null

  var completionVariableCache: CompletionVariableCache? = null

  var locationSource: LocationSource = BestLocation()

  private var eventLoopGroup: NioEventLoopGroup? = null

  private val closeables = Lists.newArrayList<Closeable>()

  private fun versionString(): String {
    return "1"
  }

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
        PrintCredentials(client!!, credentialsStore!!, outputHandler!!,
            serviceInterceptor!!).showCredentials(arguments) { this.createRequestBuilder() }
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
        showApiDocs()
        return 0
      }

      if (authorize) {
        authorize()
        return 0
      }

      if (renew) {
        renew()
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

  @Throws(Exception::class)
  private fun showApiDocs() {
    val docs = ServiceApiDocPresenter(serviceInterceptor!!, client!!, credentialsStore!!)

    getFullCompletionUrl()?.let { u ->
      docs.explainApi(u, outputHandler!!, client!!)
    }
  }

  // TODO refactor this mess out of Main
  @Throws(Exception::class)
  private fun urlCompletionList(): String {
    val command = getShellCommand()

    val commandCompletor = command.completer()
    if (commandCompletor != null) {
      val urls = commandCompletion(commandCompletor, arguments)

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
      val urls = completer.urlList(fullCompletionUrl)

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

  @Throws(IOException::class)
  private fun commandCompletion(urlCompleter: ArgumentCompleter, arguments: List<String>): UrlList {
    return urlCompleter.urlList(arguments[arguments.size - 1])
  }

  /*
 * The last url in arguments which should be used for completion or apidoc requests.
 * In the case of javascript command expansion, it is expanded first before
 * being returned.
 *
 * n.b. arguments may be modified by this call.
 */
  @Throws(Exception::class)
  private fun getFullCompletionUrl(): String? {
    if (arguments.isEmpty()) {
      return null
    }

    var urlToComplete = arguments[arguments.size - 1]

    val command = getShellCommand()

    if (command is JavascriptApiCommand) {
      val requests = command.buildRequests(client!!, requestBuilder!!, arguments)

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

    requestBuilder = createRequestBuilder()

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

    val requests = command.buildRequests(client!!, requestBuilder!!, arguments)

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
    val responses = Lists.newArrayList<PotentialResponse>()

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

    try {
      return SuccessfulResponse(client.newCall(request).await())
    } catch (ioe: IOException) {
      return FailedResponse(ioe)
    }
  }

  @Throws(Exception::class)
  private fun authorize() {
    authorisation!!.authorize(findAuthInterceptor(), token, arguments)
  }

  @Throws(Exception::class)
  private fun renew() {
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
      dnsMode === DnsMode.NETTY -> NettyDns.byName(ipMode, getEventLoopGroup(), this.dnsServers!!)
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

  private fun getEventLoopGroup(): NioEventLoopGroup {
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

    val keyManagers = Lists.newArrayList<KeyManager>()

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
      val trustManagers = Lists.newArrayList<X509TrustManager>()

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

  fun createRequestBuilder(): Request.Builder {
    val requestBuilder = Request.Builder()

    val headerMap = HeaderUtil.headerMap(headers?.toList()).toMutableMap()

    requestBuilder.method(getRequestMethod(), getRequestBody(headerMap))

    if (headers != null) {
      headerMap.forEach { k, v -> requestBuilder.header(k, v) }
    }
    if (referer != null) {
      requestBuilder.header("Referer", referer!!)
    }
    requestBuilder.header("User-Agent", userAgent)

    return requestBuilder
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