package com.baulsupp.okurl.commands

import brave.Tracing
import brave.http.HttpTracing
import brave.propagation.TraceContext
import brave.sampler.Sampler
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.DownloadHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.authenticator.Authorisation
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.RenewingInterceptor
import com.baulsupp.okurl.brotli.BrotliInterceptor
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenSet
import com.baulsupp.okurl.location.BestLocation
import com.baulsupp.okurl.location.LocationSource
import com.baulsupp.okurl.network.DnsMode
import com.baulsupp.okurl.network.DnsOverride
import com.baulsupp.okurl.network.DnsSelector
import com.baulsupp.okurl.network.GoogleDns
import com.baulsupp.okurl.network.IPvMode
import com.baulsupp.okurl.network.InterfaceSocketFactory
import com.baulsupp.okurl.network.NettyDns
import com.baulsupp.okurl.network.dnsoverhttps.DohProviders
import com.baulsupp.okurl.okhttp.CipherSuiteOption
import com.baulsupp.okurl.okhttp.ConnectionSpecOption
import com.baulsupp.okurl.okhttp.OkHttpResponseExtractor
import com.baulsupp.okurl.okhttp.TlsVersionOption
import com.baulsupp.okurl.okhttp.defaultConnectionSpec
import com.baulsupp.okurl.preferences.Preferences
import com.baulsupp.okurl.security.BasicPromptAuthenticator
import com.baulsupp.okurl.security.CertificatePin
import com.baulsupp.okurl.security.CertificateUtils
import com.baulsupp.okurl.security.ConsoleCallbackHandler
import com.baulsupp.okurl.security.InsecureHostnameVerifier
import com.baulsupp.okurl.security.InsecureTrustManager
import com.baulsupp.okurl.security.KeystoreUtils
import com.baulsupp.okurl.security.OpenSCUtil
import com.baulsupp.okurl.services.ServiceLibrary
import com.baulsupp.okurl.services.twitter.TwitterCachingInterceptor
import com.baulsupp.okurl.tracing.TracingMode
import com.baulsupp.okurl.tracing.UriTransportRegistry
import com.baulsupp.okurl.tracing.ZipkinConfig
import com.baulsupp.okurl.tracing.ZipkinTracingInterceptor
import com.baulsupp.okurl.tracing.ZipkinTracingListener
import com.baulsupp.okurl.util.ClientException
import com.baulsupp.okurl.util.InetAddressParam
import com.baulsupp.okurl.util.LoggingUtil
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.github.markusbernhardt.proxy.ProxySearch
import com.github.rvesse.airline.HelpOption
import com.github.rvesse.airline.annotations.Arguments
import com.github.rvesse.airline.annotations.Option
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues
import com.moczul.ok2curl.CurlInterceptor
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import okhttp3.unixdomainsockets.UnixDomainSocketFactory
import zipkin2.Span
import zipkin2.reporter.Reporter
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.net.Proxy
import java.security.KeyStore
import java.time.LocalTime
import java.util.ArrayList
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.net.SocketFactory
import javax.net.ssl.KeyManager
import javax.net.ssl.X509TrustManager

abstract class CommandLineClient : ToolSession {

  @Option(name = ["--user-agent"], description = "User-Agent to send to server")
  var userAgent = Main.NAME + "/" + versionString()

  @Option(
    name = ["--connect-timeout"],
    description = "Maximum time allowed for connection (seconds). (0 = disabled)"
  )
  var connectTimeout: Int = 5

  @Option(
    name = ["--read-timeout"],
    description = "Maximum time allowed for reading data (seconds). (0 = disabled)"
  )
  var readTimeout: Int = 20

  @Option(name = ["--ping-interval"], description = "Interval between pings. (0 = disabled)")
  var pingInterval: Int = 5

  @Option(name = ["-k", "--insecure"], description = "Allow connections to SSL sites without certs")
  var allowInsecure = false

  @Option(name = ["-i", "--include"], description = "Include protocol headers in the output")
  var showHeaders = false

  @Option(name = ["--frames"], description = "Log HTTP/2 frames to STDERR")
  var showHttp2Frames = false

  @Option(name = ["--debug"], description = "Debug")
  var debug = false

  @Option(name = ["-V", "--version"], description = "Show version number and quit")
  var version = false

  @Option(name = ["--cache"], description = "Cache directory")
  var cacheDirectory: File? = null

  @Option(name = ["--protocols"], description = "Protocols")
  var protocols: String? = null

  @Option(name = ["--tracing"], description = "Activate Zipkin Tracing")
  var tracing: TracingMode? = null

  @Option(name = ["--ip"], description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)")
  @AllowedRawValues(allowedValues = ["system", "ipv4", "ipv6", "ipv4only", "ipv6only"])
  var ipMode = IPvMode.SYSTEM

  @Option(name = ["--dns"], description = "DNS (netty, java, dnsoverhttps)")
  @AllowedRawValues(allowedValues = ["java", "netty", "dnsoverhttps"])
  var dnsMode = DnsMode.JAVA

  @Option(name = ["--dnsServers"], description = "Specific DNS Servers (csv, google)")
  var dnsServers: String? = null

  @Option(name = ["--resolve"], description = "DNS Overrides (HOST:TARGET)")
  var resolve: List<String>? = null

  @Option(name = ["--certificatePin"], description = "Certificate Pin to define host:pinsha")
  var certificatePins: List<CertificatePin>? = null

  @Option(name = ["--networkInterface"], description = "Specific Local Network Interface")
  var networkInterface: String? = null

  @Option(name = ["--clientauth"], description = "Use Client Authentication (from keystore)")
  var clientAuth = false

  @Option(name = ["--keystore"], description = "Keystore")
  var keystoreFile: File? = null

  @Option(name = ["--unixSocket"], description = "Unix Socket")
  var unixSocket: File? = null

  @Option(name = ["--cert"], description = "Use given server cert (Root CA)")
  var serverCerts: MutableList<File>? = null

  @Option(
    name = ["--connectionSpec"],
    description = "Connection Spec (RESTRICTED_TLS, MODERN_TLS, COMPATIBLE_TLS)"
  )
  var connectionSpec: ConnectionSpecOption = defaultConnectionSpec()

  @Option(name = ["--cipherSuite"], description = "Cipher Suites")
  var cipherSuites: MutableList<CipherSuiteOption>? = null

  @Option(name = ["--tlsVersions"], description = "TLS Versions")
  var tlsVersions: MutableList<TlsVersionOption>? = null

  @Option(name = ["--opensc"], description = "Send OpenSC Client Certificate (slot)")
  var opensc: Int? = null

  @Option(name = ["--socks"], description = "Use SOCKS proxy")
  var socksProxy: InetAddressParam? = null

  @Option(name = ["--proxy"], description = "Use HTTP proxy")
  var proxy: InetAddressParam? = null

  @Option(name = ["--os-proxy"], description = "Use OS defined proxy")
  var osProxy: Boolean = false

  @Option(name = ["-s", "--set"], description = "Token Set e.g. work")
  var tokenSet: String? = null

  @Option(name = ["--ssldebug"], description = "SSL Debug")
  var sslDebug: Boolean = false

  @Option(name = ["--user"], description = "user:password for basic auth")
  var user: String? = null

  @Option(name = ["--maxrequests"], description = "Concurrency Level")
  var maxRequests = 16

  @Option(name = ["--curl"], description = "Show curl commands")
  var curl = false

  @Option(name = ["-r", "--raw"], description = "Raw Output")
  var rawOutput = false

  @Option(name = ["--localCerts"], description = "Local Certificates")
  var localCerts: File? = File(System.getenv("INSTALLDIR") ?: ".", "certificates")

  @Arguments(title = ["arguments"], description = "Remote resource URLs")
  var arguments: MutableList<String> = ArrayList()

  lateinit var authenticatingInterceptor: AuthenticatingInterceptor

  lateinit var renewingInterceptor: RenewingInterceptor

  lateinit var authorisation: Authorisation

  override lateinit var client: OkHttpClient

  override lateinit var outputHandler: OutputHandler<Response>

  override lateinit var credentialsStore: CredentialsStore

  override lateinit var locationSource: LocationSource

  override val defaultTokenSet: TokenSet?
    get() = tokenSet?.let { TokenSet(it) }

  override var serviceLibrary: ServiceLibrary = object : ServiceLibrary {
    override fun knownServices(): Set<String> {
      return authenticatingInterceptor.names().toSortedSet()
    }

    override val services: Iterable<AuthInterceptor<*>>
      get() = authenticatingInterceptor.services

    override fun findAuthInterceptor(name: String): AuthInterceptor<*>? =
      authenticatingInterceptor.findAuthInterceptor(name)
  }

  override fun close() {
    for (c in closeables) {
      try {
        c.close()
      } catch (e: Exception) {
        Platform.get().log(Platform.INFO, "close failed", e)
      }
    }
  }

  lateinit var preferences: Preferences

  var eventLoopGroup: NioEventLoopGroup? = null

  val closeables = mutableListOf<Closeable>()

  abstract val help: HelpOption<*>?

  fun buildDns(builder: OkHttpClient.Builder): Dns {
    val dns = when (dnsMode) {
      DnsMode.NETTY -> NettyDns.byName(ipMode, createEventLoopGroup(), this.dnsServers ?: "8.8.8.8")
      DnsMode.GOOGLE -> DnsSelector(ipMode, GoogleDns.build({ client }, ipMode))
      DnsMode.DNSOVERHTTPS -> DnsSelector(ipMode, DohProviders.buildGoogle(builder.build()))
      DnsMode.JAVA -> {
        if (dnsServers != null) {
          throw UsageException("unable to set dns servers with java DNS")
        }

        DnsSelector(ipMode, Dns.SYSTEM)
      }
    }
    if (resolve != null) {
      return DnsOverride.build(dns, resolve!!)
    }
    return dns
  }

  fun createEventLoopGroup(): NioEventLoopGroup {
    if (eventLoopGroup == null) {
      val threadFactory: ThreadFactory = DefaultThreadFactory("netty", true)
      eventLoopGroup = NioEventLoopGroup(5, threadFactory)

      closeables.add(Closeable { eventLoopGroup!!.shutdownGracefully(0, 0, TimeUnit.SECONDS) })
    }

    return eventLoopGroup!!
  }

  fun getSocketFactory(): SocketFactory =
    InterfaceSocketFactory.byName(networkInterface!!)
      ?: throw UsageException("networkInterface '$networkInterface' not found")

  fun configureTls(builder: OkHttpClient.Builder) {
    val callbackHandler = ConsoleCallbackHandler()

    // possibly null
    var keystore: KeyStore? = null

    if (keystoreFile != null) {
      keystore = KeystoreUtils.getKeyStore(keystoreFile)
    }

    if (cipherSuites != null || tlsVersions != null) {
      val specBuilder = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)

      if (cipherSuites != null) {
        specBuilder.cipherSuites(*(cipherSuites!!.map { it.suite }.toTypedArray()))
      }

      if (tlsVersions != null) {
        specBuilder.tlsVersions(*(tlsVersions!!.map { it.version }.toTypedArray()))
      }

      builder.connectionSpecs(listOf(specBuilder.build(), ConnectionSpec.CLEARTEXT))
    } else {
      builder.connectionSpecs(connectionSpec.specs.asList() + ConnectionSpec.CLEARTEXT)
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
      trustManager = InsecureTrustManager
      builder.hostnameVerifier(InsecureHostnameVerifier)
    } else {
      val trustManagers = mutableListOf<X509TrustManager>()

      if (keystore != null) {
        trustManagers.add(CertificateUtils.trustManagerForKeyStore(keystore))
      }

      if (serverCerts != null) {
        trustManagers.add(CertificateUtils.load(serverCerts!!.toList()))
      }

      trustManager = CertificateUtils.combineTrustManagers(trustManagers, includedDir = localCerts)
    }

    builder.sslSocketFactory(
      KeystoreUtils.createSslSocketFactory(
        KeystoreUtils.keyManagerArray(keyManagers),
        trustManager
      ),
      trustManager
    )

    if (certificatePins != null) {
      builder.certificatePinner(CertificatePin.buildFromCommandLine(certificatePins!!.toList()))
    }
  }

  fun versionString(): String {
    return this.javaClass.`package`.implementationVersion ?: "dev"
  }

  suspend fun run(): Int {
    if (help?.showHelpIfRequested() == true) {
      return 0
    }

    initialise()

    if (version) {
      outputHandler.info(name() + " " + versionString())
      return 0
    }

    return try {
      runCommand(arguments)
    } catch (e: ClientException) {
      outputHandler.showError(e.message)
      -1
    } catch (e: UsageException) {
      outputHandler.showError(e.message)
      -1
    } catch (e: Exception) {
      outputHandler.showError("unknown error", e)
      -2
    } finally {
      for (c in closeables) {
        try {
          c.close()
        } catch (e: Exception) {
          Platform.get().log(Platform.INFO, "close failed", e)
        }
      }
    }
  }

  abstract fun name(): String

  open fun runCommand(runArguments: List<String>): Int {
    return 0
  }

  open fun initialise() {
    System.setProperty("apple.awt.UIElement", "true")
    LoggingUtil.configureLogging(debug, showHttp2Frames, sslDebug)

    closeables.add(Closeable {
      if (this::client.isInitialized) {
        client.dispatcher().executorService().shutdown()
        client.connectionPool().evictAll()
      }
    })

    if (!this::preferences.isInitialized) {
      preferences = Preferences.local
    }

    if (!this::outputHandler.isInitialized) {
      outputHandler = buildHandler()
    }

    if (!this::locationSource.isInitialized) {
      locationSource = BestLocation(outputHandler)
    }

    if (!this::credentialsStore.isInitialized) {
      credentialsStore = CredentialFactory.createCredentialsStore()
    }

    if (!this::authenticatingInterceptor.isInitialized) {
      authenticatingInterceptor = AuthenticatingInterceptor(this.credentialsStore)
    }

    if (!this::renewingInterceptor.isInitialized) {
      renewingInterceptor = RenewingInterceptor(this.credentialsStore)
    }

    if (!this::authorisation.isInitialized) {
      authorisation = Authorisation(this)
    }

    val clientBuilder = createClientBuilder()

    client = clientBuilder.build()
  }

  open fun createClientBuilder(): OkHttpClient.Builder {
    val builder = OkHttpClient.Builder()
    builder.connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
    builder.readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
    builder.pingInterval(pingInterval.toLong(), TimeUnit.SECONDS)

    if (networkInterface != null) {
      builder.socketFactory(getSocketFactory())
    } else if (unixSocket != null) {
      builder.socketFactory(UnixDomainSocketFactory(unixSocket))
    }

    configureTls(builder)

    if (cacheDirectory != null) {
      builder.cache(Cache(cacheDirectory!!, (64 * 1024 * 1024).toLong()))
    }

    builder.addInterceptor(renewingInterceptor)

    // TODO move behind AuthInterceptor API
    builder.addNetworkInterceptor(TwitterCachingInterceptor())
    builder.addNetworkInterceptor(BrotliInterceptor)

    if (debug) {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
      builder.addNetworkInterceptor(loggingInterceptor)
    }

    applyProxy(builder)

    val authenticatorBuilder = DispatchingAuthenticator.Builder()

    if (user != null) {
      val userParts = user!!.split(":".toRegex(), 2)
      if (userParts.size < 2) {
        throw UsageException("--user should have user:password")
      }

      val credentials = BasicCredentials(userParts[0], userParts[1])
      authenticatorBuilder.with("basic", BasicPromptAuthenticator(credentials))
    } else {
      authenticatorBuilder.with("basic", BasicPromptAuthenticator())
    }

    val authenticator = authenticatorBuilder.build()

    builder.authenticator(authenticator)
    builder.proxyAuthenticator(authenticator)

    // TODO add caching
//    val authCache = ConcurrentHashMap<String, CachingAuthenticator>()
//    builder.authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
//    builder.addInterceptor(AuthenticationCacheInterceptor(authCache))

    protocols?.let {
      builder.protocols(protocolList(it))
    }

    val dispatcher = Dispatcher()
    dispatcher.maxRequests = maxRequests
    dispatcher.maxRequestsPerHost = maxRequests
    builder.dispatcher(dispatcher)

    val connectionPool = ConnectionPool()
    builder.connectionPool(connectionPool)

    applyTracing(builder)

    builder.addNetworkInterceptor(authenticatingInterceptor)

    if (curl) {
      builder.addNetworkInterceptor(CurlInterceptor(System.err::println))
    }

//    builder.dns(buildDns(builder))

    return builder
  }

  private fun applyProxy(builder: OkHttpClient.Builder) {
    when {
      osProxy -> builder.proxySelector(ProxySearch.getDefaultProxySearch().proxySelector)
      socksProxy != null -> builder.proxy(Proxy(Proxy.Type.SOCKS, socksProxy!!.address))
      proxy != null -> builder.proxy(Proxy(Proxy.Type.HTTP, proxy!!.address))
      preferences.osProxy == true -> builder.proxySelector(ProxySearch.getDefaultProxySearch().proxySelector)
      preferences.proxy != null -> builder.proxy(preferences.proxy!!.build())
    }
  }

  private fun protocolList(it: String): List<Protocol> = it.split(",").map {
    Protocol.get(it)
  }.let {
    if (it.contains(Protocol.HTTP_1_1)) it else it + Protocol.HTTP_1_1
  }

  private fun applyTracing(clientBuilder: OkHttpClient.Builder) {
    tracing = tracing ?: preferences.tracing

    if (tracing == null)
      return

    if (tracing == TracingMode.CONSOLE) {
      val logger = HttpLoggingInterceptor.Logger { message -> println(message) }
      clientBuilder.eventListenerFactory(LoggingEventListener.Factory(logger))
      return
    }

    val config = ZipkinConfig.load()
    val zipkinSenderUri = config.zipkinSenderUri()
    val reporter: Reporter<Span>

    reporter = if (zipkinSenderUri != null) {
      UriTransportRegistry.forUri(zipkinSenderUri)
    } else {
      Reporter.CONSOLE
    }

    val tracing = Tracing.newBuilder()
      .localServiceName("okurl")
      .spanReporter(reporter)
      .sampler(Sampler.ALWAYS_SAMPLE)
      .build()

    val httpTracing = HttpTracing.create(tracing)

    val tracer = tracing.tracer()

    val opener: Consumer<TraceContext> = Consumer { tc ->
      closeables.add(Closeable {
        val link = config.openFunction().invoke(tc)

        if (link != null) {
          runBlocking {
            openLink(link)
          }
        }
      })
    }

    clientBuilder.eventListenerFactory { call ->
      ZipkinTracingListener(call, tracer, httpTracing, opener, this.tracing == TracingMode.ZIPKIN_TRACING)
    }

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

  suspend fun openLink(link: String) {
    try {
      outputHandler.openLink(link)
    } catch (e: IOException) {
      outputHandler.showError("Can't open link", e)
    }
  }

  open fun buildHandler(): OutputHandler<Response> = when {
    rawOutput -> DownloadHandler(OkHttpResponseExtractor(), File("-"))
    else -> ConsoleHandler.instance(OkHttpResponseExtractor())
  }

  fun token(): Token {
    return tokenSet?.let {
      TokenSet(it)
    } ?: DefaultToken
  }
}
