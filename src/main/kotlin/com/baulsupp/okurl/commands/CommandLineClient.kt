package com.baulsupp.okurl.commands

import brave.Tracing
import brave.http.HttpTracing
import brave.propagation.TraceContext
import brave.sampler.Sampler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.output.handler.ConsoleHandler
import com.baulsupp.oksocial.output.handler.DownloadHandler
import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.OkUrl
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.authenticator.Authorisation
import com.baulsupp.okurl.authenticator.RenewingInterceptor
import com.baulsupp.okurl.commands.converters.CipherSuiteConverter
import com.baulsupp.okurl.commands.converters.DnsConverter
import com.baulsupp.okurl.commands.converters.IPvModeConverter
import com.baulsupp.okurl.commands.converters.TlsVersionConverter
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenSet
import com.baulsupp.okurl.location.BestLocation
import com.baulsupp.okurl.location.LocationSource
import com.baulsupp.okurl.moshi.Rfc3339InstantJsonAdapter
import com.baulsupp.okurl.network.DnsMode
import com.baulsupp.okurl.network.DnsOverride
import com.baulsupp.okurl.network.DnsSelector
import com.baulsupp.okurl.network.GoogleDns
import com.baulsupp.okurl.network.IPvMode
import com.baulsupp.okurl.network.InterfaceSocketFactory
import com.baulsupp.okurl.network.dnsoverhttps.DohProviders
import com.baulsupp.okurl.okhttp.ConnectionSpecOption
import com.baulsupp.okurl.okhttp.ConnectionSpecOption.MODERN_TLS_13
import com.baulsupp.okurl.okhttp.OkHttpResponseExtractor
import com.baulsupp.okurl.okhttp.WireSharkListenerFactory
import com.baulsupp.okurl.okhttp.WireSharkListenerFactory.WireSharkKeyLoggerListener.Launch
import com.baulsupp.okurl.preferences.Preferences
import com.baulsupp.okurl.security.CertificatePin
import com.baulsupp.okurl.security.ConsoleCallbackHandler
import com.baulsupp.okurl.security.OpenSCUtil
import com.baulsupp.okurl.services.ServiceLibrary
import com.baulsupp.okurl.services.mapbox.model.MapboxLatLongAdapter
import com.baulsupp.okurl.tracing.TracingMode
import com.baulsupp.okurl.tracing.UriTransportRegistry
import com.baulsupp.okurl.tracing.ZipkinConfig
import com.baulsupp.okurl.tracing.ZipkinTracingInterceptor
import com.baulsupp.okurl.tracing.ZipkinTracingListener
import com.baulsupp.okurl.util.ClientException
import com.baulsupp.okurl.util.InetAddressParam
import com.baulsupp.okurl.util.LoggingUtil
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.basic.BasicAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.EventListener
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.brotli.BrotliInterceptor
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import okhttp3.tls.HandshakeCertificates
import picocli.CommandLine
import picocli.CommandLine.Option
import zipkin2.Span
import zipkin2.reporter.Reporter
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.net.Proxy
import java.security.SecureRandom
import java.util.ArrayList
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.net.SocketFactory
import javax.net.ssl.SSLContext

abstract class CommandLineClient : ToolSession, Runnable {
  @Option(names = ["--user-agent"], description = ["User-Agent to send to server"])
  var userAgent = "${Main.NAME}/${versionString()} OkHttp/${OkHttp.VERSION}"

  @Option(
    names = ["--connect-timeout"],
    description = ["Maximum time allowed for connection (seconds). (0 = disabled)"]
  )
  var connectTimeout: Int = 5

  @Option(
    names = ["--read-timeout"],
    description = ["Maximum time allowed for reading data (seconds). (0 = disabled)"]
  )
  var readTimeout: Int = 20

  @Option(names = ["--ping-interval"], description = ["Interval between pings. (0 = disabled)"])
  var pingInterval: Int = 5

  @Option(names = ["-k", "--insecure"],
    description = ["Allow connections to SSL sites without certs"])
  var insecureHost: List<String>? = null

  @Option(names = ["-i", "--include"], description = ["Include protocol headers in the output"])
  var showHeaders = false

  @Option(names = ["--frames"], description = ["Log HTTP/2 frames to STDERR"])
  var showHttp2Frames = false

  @Option(names = ["--debug"], description = ["Debug"])
  var debug = false

  @Option(names = ["--cache"], description = ["Cache directory"])
  var cacheDirectory: File? = null

  @Option(names = ["--protocols"], description = ["Protocols"])
  var protocols: String? = null

  @Option(names = ["--tracing"], description = ["Activate Zipkin Tracing"])
  var tracing: TracingMode? = null

  @Option(names = ["--ip"],
    description = ["IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)"], converter = [IPvModeConverter::class])
  var ipMode = IPvMode.SYSTEM

  @Option(names = ["--dns"], description = ["DNS (netty, java, dnsoverhttps)"], converter = [DnsConverter::class])
  var dnsMode = DnsMode.JAVA

  @Option(names = ["--dnsServers"], description = ["Specific DNS Servers (csv, google)"])
  var dnsServers: String? = null

  @Option(names = ["--resolve"], description = ["DNS Overrides (HOST:TARGET)"])
  var resolve: List<String>? = null

  @Option(names = ["--certificatePin"], description = ["Certificate Pin to define host:pinsha"])
  var certificatePins: List<CertificatePin>? = null

  @Option(names = ["--networkInterface"], description = ["Specific Local Network Interface"])
  var networkInterface: String? = null

  @Option(names = ["--wireshark"], description = ["Activate Wireshark"])
  var wireshark: Boolean = false

  @Option(
    names = ["--connectionSpec"],
    description = ["Connection Spec (RESTRICTED_TLS, MODERN_TLS, COMPATIBLE_TLS)"]
  )
  var connectionSpec: ConnectionSpecOption = MODERN_TLS_13

  @Option(names = ["--cipherSuite"], description = ["Cipher Suites"], converter = [CipherSuiteConverter::class])
  var cipherSuites: MutableList<CipherSuite>? = null

  @Option(names = ["--tlsVersions"], description = ["TLS Versions"], converter = [TlsVersionConverter::class])
  var tlsVersions: MutableList<TlsVersion>? = null

  @Option(names = ["--socks"], description = ["Use SOCKS proxy"])
  var socksProxy: InetAddressParam? = null

  @Option(names = ["--proxy"], description = ["Use HTTP proxy"])
  var proxy: InetAddressParam? = null

  @Option(names = ["-s", "--set"], description = ["Token Set e.g. work"])
  var tokenSet: String? = null

  @Option(names = ["--ssldebug"], description = ["SSL Debug"])
  var sslDebug: Boolean = false

  @Option(names = ["--user"], description = ["user:password for basic auth"])
  var user: String? = null

  @Option(names = ["--maxrequests"], description = ["Concurrency Level"])
  var maxRequests = 16

  @Option(names = ["-r", "--raw"], description = ["Raw Output"])
  var rawOutput = false

  @Option(names = ["--localCerts"], description = ["Local Certificates"])
  var localCerts: File? = File(System.getenv("INSTALLDIR") ?: ".", "certificates")

  @Option(names = ["--opensc"], description = ["Use OpenSC key manager"])
  var opensc = false

  @CommandLine.Parameters(paramLabel = "arguments", description = ["Remote resource URLs"])
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
        Platform.get().log("close failed", Platform.INFO, e)
      }
    }
  }

  lateinit var preferences: Preferences

  val closeables = mutableListOf<Closeable>()

  fun buildDns(builder: OkHttpClient.Builder): Dns {
    val dns = when (dnsMode) {
      DnsMode.GOOGLE -> DnsSelector(ipMode, GoogleDns.build({ client }, ipMode))
      DnsMode.DNSOVERHTTPS -> DnsSelector(ipMode, DohProviders.buildCloudflare(builder.build()))
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

  fun getSocketFactory(): SocketFactory =
    InterfaceSocketFactory.byName(networkInterface!!)
      ?: throw UsageException("networkInterface '$networkInterface' not found")

  fun configureTls(builder: OkHttpClient.Builder) {
    if (cipherSuites != null || tlsVersions != null) {
      val specBuilder = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).apply {
        if (cipherSuites != null) {
          cipherSuites(*(cipherSuites!!.toTypedArray()))
        }

        if (tlsVersions != null) {
          tlsVersions(*(tlsVersions!!.toTypedArray()))
        }
      }
      builder.connectionSpecs(listOf(specBuilder.build(), ConnectionSpec.CLEARTEXT))
    } else {
      builder.connectionSpecs(connectionSpec.specs.asList() + ConnectionSpec.CLEARTEXT)
    }

    // possibly null
    val handshakeCertificatesBuilder = HandshakeCertificates.Builder()
      .addPlatformTrustedCertificates()

    for (host in insecureHost.orEmpty()) {
      handshakeCertificatesBuilder.addInsecureHost(host)
    }

    // TODO local key and root CA files
    val handshakeCertificates = handshakeCertificatesBuilder.build()

    val sslSocketFactory = if (opensc) {
      val keyManager = OpenSCUtil.getKeyManager(ConsoleCallbackHandler)

      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(arrayOf(keyManager), arrayOf(handshakeCertificates.trustManager), SecureRandom())

      sslContext.socketFactory
    } else {
      handshakeCertificates.sslSocketFactory()
    }

    builder.sslSocketFactory(
      sslSocketFactory,
      handshakeCertificates.trustManager
    )

    if (certificatePins != null) {
      builder.certificatePinner(CertificatePin.buildFromCommandLine(certificatePins!!.toList()))
    }
  }

  override fun run() {
    runBlocking {
      exec()
    }
  }

  suspend fun exec(): Int {
    initialise()

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
          Platform.get().log("close failed", Platform.INFO, e)
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
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
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

    Main.client = client
    Main.moshi = Moshi.Builder()
      .add(MapboxLatLongAdapter())
      .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
      .add(Rfc3339InstantJsonAdapter())
      .build()!!
  }

  open fun createClientBuilder(): OkHttpClient.Builder {
    val builder = OkHttpClient.Builder()
    builder.connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
    builder.readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
    builder.pingInterval(pingInterval.toLong(), TimeUnit.SECONDS)

    if (networkInterface != null) {
      builder.socketFactory(getSocketFactory())
    }

    if (wireshark) {
      if (tlsVersions == null) {
        tlsVersions = mutableListOf(TlsVersion.TLS_1_2)
      }

      val wiresharkListener = WireSharkListenerFactory(
        logFile = File("/tmp/key.log"), launch = Launch.CommandLine)
      builder.eventListenerFactory(wiresharkListener)

      val process = wiresharkListener.launchWireShark()
      closeables.add(Closeable {
        Thread.sleep(250)
        process?.destroyForcibly()
      })
    } else {
      applyTracing(builder)
    }

    configureTls(builder)

    if (cacheDirectory != null) {
      builder.cache(ApiCompleter.cache)
    }

    builder.addInterceptor(renewingInterceptor)

    // TODO move behind AuthInterceptor API
    builder.addInterceptor(BrotliInterceptor)

    if (debug) {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
      builder.addNetworkInterceptor(loggingInterceptor)
    }

    applyProxy(builder)

    if (user != null) {
      val userParts = user!!.split(":".toRegex(), 2)
      if (userParts.size < 2) {
        throw UsageException("--user should have user:password")
      }

      val authCache = ConcurrentHashMap<String, CachingAuthenticator>()

      val credentials = Credentials(userParts[0], userParts[1])
      val basicAuthenticator = BasicAuthenticator(credentials)
      val digestAuthenticator = DigestAuthenticator(credentials)

      val authenticator: DispatchingAuthenticator = DispatchingAuthenticator.Builder()
        .with("digest", digestAuthenticator)
        .with("basic", basicAuthenticator)
        .build()

      val cachingAuthenticator = CachingAuthenticatorDecorator(authenticator, authCache)

      builder.authenticator(cachingAuthenticator)
      builder.addInterceptor(AuthenticationCacheInterceptor(authCache))
      builder.proxyAuthenticator(cachingAuthenticator)
    }

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

    builder.addNetworkInterceptor(authenticatingInterceptor)

    builder.dns(buildDns(builder))

    return builder
  }

  private fun applyProxy(builder: OkHttpClient.Builder) {
    when {
      socksProxy != null -> builder.proxy(Proxy(Proxy.Type.SOCKS, socksProxy!!.address))
      proxy != null -> builder.proxy(Proxy(Proxy.Type.HTTP, proxy!!.address))
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
      val logger = object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
          println(message)
        }
      }
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

    clientBuilder.eventListenerFactory(object : EventListener.Factory {
      override fun create(call: Call): EventListener =
        ZipkinTracingListener(call, tracer, httpTracing, opener,
          this@CommandLineClient.tracing == TracingMode.ZIPKIN_TRACING)
    })

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

  open fun buildHandler(): OutputHandler<Response> {
    return when {
      rawOutput -> DownloadHandler(OkHttpResponseExtractor(), File("-"))
      else -> ConsoleHandler.instance(OkHttpResponseExtractor())
    }
  }

  fun token(): Token {
    return tokenSet?.let {
      TokenSet(it)
    } ?: DefaultToken
  }

  companion object {
    fun versionString(): String {
      return OkUrl.VERSION
    }
  }
}
