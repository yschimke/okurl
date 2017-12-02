package com.baulsupp.oksocial

import brave.Tracing
import brave.http.HttpTracing
import brave.internal.Platform
import brave.propagation.TraceContext
import brave.sampler.Sampler
import com.baulsupp.oksocial.authenticator.AuthInterceptor.Companion.logger
import com.baulsupp.oksocial.authenticator.Authorisation
import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.OSXCredentialsStore
import com.baulsupp.oksocial.credentials.PreferencesCredentialsStore
import com.baulsupp.oksocial.location.BestLocation
import com.baulsupp.oksocial.location.LocationSource
import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.DnsOverride
import com.baulsupp.oksocial.network.DnsSelector
import com.baulsupp.oksocial.network.GoogleDns
import com.baulsupp.oksocial.network.IPvMode
import com.baulsupp.oksocial.network.InterfaceSocketFactory
import com.baulsupp.oksocial.network.NettyDns
import com.baulsupp.oksocial.okhttp.OkHttpResponseExtractor
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
import com.baulsupp.oksocial.util.ClientException
import com.baulsupp.oksocial.util.InetAddressParam
import com.baulsupp.oksocial.util.LoggingUtil
import com.baulsupp.oksocial.util.ProtocolUtil
import com.moczul.ok2curl.CurlInterceptor
import io.airlift.airline.Arguments
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.IOUtils
import zipkin.Span
import zipkin.reporter.Reporter
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.net.Proxy
import java.security.KeyStore
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.net.SocketFactory
import javax.net.ssl.KeyManager
import javax.net.ssl.X509TrustManager

open class CommandLineClient : HelpOption() {

  @Option(name = ["-A", "--user-agent"], description = "User-Agent to send to server")
  var userAgent = Main.NAME + "/" + versionString()

  @Option(name = ["--connect-timeout"], description = "Maximum time allowed for connection (seconds)")
  var connectTimeout: Int? = null

  @Option(name = ["--read-timeout"], description = "Maximum time allowed for reading data (seconds)")
  var readTimeout: Int? = null

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

  @Option(name = ["--zipkin", "-z"], description = "Activate Zipkin Tracing")
  var zipkin = false

  @Option(name = ["--zipkinTrace"], description = "Activate Detailed Zipkin Tracing")
  var zipkinTrace = false

  @Option(name = ["--ip"], description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)", allowedValues = ["system", "ipv4", "ipv6", "ipv4only", "ipv6only"])
  var ipMode = IPvMode.SYSTEM

  @Option(name = ["--dns"], description = "DNS (netty, java)", allowedValues = ["java", "netty"])
  var dnsMode = DnsMode.JAVA

  @Option(name = ["--dnsServers"], description = "Specific DNS Servers (csv, google)")
  var dnsServers: String? = null

  @Option(name = ["--resolve"], description = "DNS Overrides (HOST:TARGET)")
  var resolve: List<String>? = null

  @Option(name = ["--certificatePin"], description = "Specific Local Network Interface")
  var certificatePins: java.util.List<CertificatePin>? = null

  @Option(name = ["--networkInterface"], description = "Specific Local Network Interface")
  var networkInterface: String? = null

  @Option(name = ["--clientauth"], description = "Use Client Authentication (from keystore)")
  var clientAuth = false

  @Option(name = ["--keystore"], description = "Keystore")
  var keystoreFile: File? = null

  @Option(name = ["--cert"], description = "Use given server cert (Root CA)")
  var serverCerts: java.util.List<File>? = null

  @Option(name = ["--opensc"], description = "Send OpenSC Client Certificate (slot)")
  var opensc: Int? = null

  @Option(name = ["--socks"], description = "Use SOCKS proxy")
  var socksProxy: InetAddressParam? = null

  @Option(name = ["--proxy"], description = "Use HTTP proxy")
  var proxy: InetAddressParam? = null

  @Option(name = ["-s", "--set"], description = "Token Set e.g. work")
  var tokenSet: String? = null

  @Option(name = ["--ssldebug"], description = "SSL Debug")
  var sslDebug: Boolean = false

  @Option(name = ["--user"], description = "user:password for basic auth")
  var user: String? = null

  @Option(name = ["--maxrequests"], description = "Concurrency Level")
  val maxRequests = 16

  @Option(name = ["--curl"], description = "Show curl commands")
  var curl = false

  @Option(name = ["-r", "--raw"], description = "Raw Output")
  var rawOutput = false

  @Arguments(title = "arguments", description = "Remote resource URLs")
  var arguments: MutableList<String> = ArrayList()

  var serviceInterceptor: ServiceInterceptor? = null

  var authorisation: Authorisation? = null

  var client: OkHttpClient? = null

  var outputHandler: OutputHandler<Response>? = null

  var credentialsStore: CredentialsStore? = null

  var locationSource: LocationSource? = null

  var eventLoopGroup: NioEventLoopGroup? = null

  val closeables = mutableListOf<Closeable>()

  fun buildDns(): Dns {
    var dns: Dns
    dns = when {
      dnsMode === DnsMode.NETTY -> NettyDns.byName(ipMode, createEventLoopGroup(), this.dnsServers!!)
      dnsMode === DnsMode.DNSGOOGLE -> DnsSelector(ipMode,
              GoogleDns.fromHosts({ client!! }, ipMode, "216.58.216.142", "216.239.34.10",
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

  fun createEventLoopGroup(): NioEventLoopGroup {
    if (eventLoopGroup == null) {
      val threadFactory = DefaultThreadFactory("netty", true)
      eventLoopGroup = NioEventLoopGroup(5, threadFactory)

      closeables.add(Closeable { eventLoopGroup!!.shutdownGracefully(0, 0, TimeUnit.SECONDS) })
    }

    return eventLoopGroup!!
  }

  fun getSocketFactory(): SocketFactory =
          InterfaceSocketFactory.byName(networkInterface!!) ?: throw UsageException("networkInterface '$networkInterface' not found")

  fun configureTls(builder: OkHttpClient.Builder) {
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

  fun versionString(): String {
    return this.javaClass.`package`.implementationVersion ?: "dev"
  }

  fun run(): Int {
    if (showHelpIfRequested()) {
      return 0
    }

    initialise()

    if (version) {
      outputHandler!!.info(Main.NAME + " " + versionString())
      return 0
    }

    try {
      return runCommand(arguments)
    } catch (e: ClientException) {
      outputHandler!!.showError(e.message, null)
      return -1
    } catch (e: UsageException) {
      outputHandler!!.showError(e.message, null)
      return -1
    } catch (e: Exception) {
      outputHandler!!.showError("unknown error", e)
      return -2
    } finally {
      closeClients()
    }
  }

  open fun runCommand(runArguments: List<String>): Int {
    return 0
  }

  open fun initialise() {
    LoggingUtil.configureLogging(debug, showHttp2Frames, sslDebug)

    if (outputHandler == null) {
      outputHandler = buildHandler()
    }

    if (locationSource == null) {
      locationSource = BestLocation(outputHandler!!)
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
  }

  open fun createClientBuilder(): OkHttpClient.Builder {
    val builder = OkHttpClient.Builder()
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

  fun openLink(link: String) {
    try {
      outputHandler!!.openLink(link)
    } catch (e: IOException) {
      outputHandler!!.showError("Can't open link", e)
    }
  }

  open fun createCredentialsStore(): CredentialsStore {
    return if (PlatformUtil.isOSX) {
      OSXCredentialsStore(tokenSet)
    } else {
      PreferencesCredentialsStore(tokenSet)
    }
  }

  fun closeClients() {
    for (c in closeables) {
      IOUtils.closeQuietly(c)
    }
  }

  open fun buildHandler(): OutputHandler<Response> = when {
    rawOutput -> DownloadHandler(OkHttpResponseExtractor(), File("-"))
    else -> ConsoleHandler.instance(OkHttpResponseExtractor()) as OutputHandler<Response>
  }

  companion object {
    inline fun <reified T> fromArgs(vararg args: String): T {
      return SingleCommand.singleCommand(T::class.java).parse(*args)
    }
  }
}