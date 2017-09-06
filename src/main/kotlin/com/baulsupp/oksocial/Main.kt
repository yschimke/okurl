package com.baulsupp.oksocial

import brave.Tracer
import brave.Tracing
import brave.http.HttpTracing
import brave.internal.Platform
import brave.propagation.TraceContext
import brave.sampler.Sampler
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
import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.DownloadHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.ResponseExtractor
import com.baulsupp.oksocial.output.util.PlatformUtil
import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.security.CertificatePin
import com.baulsupp.oksocial.security.CertificateUtils
import com.baulsupp.oksocial.security.ConsoleCallbackHandler
import com.baulsupp.oksocial.security.InsecureHostnameVerifier
import com.baulsupp.oksocial.security.InsecureTrustManager
import com.baulsupp.oksocial.security.OpenSCUtil
import com.baulsupp.oksocial.services.twitter.TwitterCachingInterceptor
import com.baulsupp.oksocial.services.twitter.TwitterDeflatedResponseInterceptor
import com.baulsupp.oksocial.tracing.UriTransportRegistry
import com.baulsupp.oksocial.tracing.ZipkinConfig
import com.baulsupp.oksocial.tracing.ZipkinTracingInterceptor
import com.baulsupp.oksocial.tracing.ZipkinTracingListener
import com.baulsupp.oksocial.util.FileContent
import com.baulsupp.oksocial.util.InetAddressParam
import com.baulsupp.oksocial.util.LoggingUtil
import com.baulsupp.oksocial.util.ProtocolUtil
import com.google.common.base.Throwables
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.mcdermottroe.apple.OSXKeychainException
import com.moczul.ok2curl.CurlInterceptor
import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.airlift.airline.SingleCommand
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.net.Proxy
import java.net.SocketException
import java.security.KeyStore
import java.util.ArrayList
import java.util.Optional
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.SocketFactory
import javax.net.ssl.KeyManager
import javax.net.ssl.X509TrustManager
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.Headers
import okhttp3.HttpUrl
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

import com.baulsupp.oksocial.security.CertificateUtils.trustManagerForKeyStore
import com.baulsupp.oksocial.security.KeystoreUtils.createKeyManager
import com.baulsupp.oksocial.security.KeystoreUtils.createSslSocketFactory
import com.baulsupp.oksocial.security.KeystoreUtils.getKeyStore
import com.baulsupp.oksocial.security.KeystoreUtils.keyManagerArray
import com.baulsupp.oksocial.util.HeaderUtil.headerMap
import java.util.Arrays.asList
import java.util.Optional.empty
import java.util.Optional.ofNullable
import java.util.concurrent.TimeUnit.SECONDS
import java.util.stream.Collectors.joining

@SuppressWarnings("WeakerAccess", "CanBeFinal", "unused")
@Command(name = Main.NAME, description = "A curl for social apis.")
class Main : HelpOption() {

    @Option(name = { "-X", "--request" }, description = "Specify request command to use")
    var method: String? = null

    @Option(name = { "-d", "--data" }, description = "HTTP POST data")
    var data: String? = null

    @Option(name = { "-H", "--header" }, description = "Custom header to pass to server")
    var headers: List<String>? = null

    @Option(name = { "-A", "--user-agent" }, description = "User-Agent to send to server")
    var userAgent = NAME + "/" + versionString()

    @Option(name = "--connect-timeout", description = "Maximum time allowed for connection (seconds)")
    var connectTimeout: Integer? = null

    @Option(name = "--read-timeout", description = "Maximum time allowed for reading data (seconds)")
    var readTimeout: Integer? = null

    @Option(name = { "--no-follow" }, description = "Follow redirects")
    var dontFollowRedirects = false

    @Option(name = { "-k", "--insecure" }, description = "Allow connections to SSL sites without certs")
    var allowInsecure = false

    @Option(name = { "-i", "--include" }, description = "Include protocol headers in the output")
    var showHeaders = false

    @Option(name = "--frames", description = "Log HTTP/2 frames to STDERR")
    var showHttp2Frames = false

    @Option(name = "--debug", description = "Debug")
    var debug = false

    @Option(name = { "-e", "--referer" }, description = "Referer URL")
    var referer: String? = null

    @Option(name = { "-V", "--version" }, description = "Show version number and quit")
    var version = false

    @Option(name = { "--cache" }, description = "Cache directory")
    var cacheDirectory: File? = null

    @Option(name = { "--protocols" }, description = "Protocols")
    var protocols: String? = null

    @Option(name = { "-o", "--output" }, description = "Output file/directory")
    var outputDirectory: File? = null

    @Option(name = { "--authorize" }, description = "Authorize API")
    var authorize: Boolean = false

    @Option(name = { "--renew" }, description = "Renew API Authorization")
    var renew: Boolean = false

    @Option(name = { "--token" }, description = "Use existing Token for authorization")
    var token: String? = null

    @Option(name = { "--curl" }, description = "Show curl commands")
    var curl = false

    @Option(name = { "--zipkin", "-z" }, description = "Activate Zipkin Tracing")
    var zipkin = false

    @Option(name = { "--zipkinTrace" }, description = "Activate Detailed Zipkin Tracing")
    var zipkinTrace = false

    @Option(name = { "--ip" }, description = "IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)", allowedValues = { "system", "ipv4", "ipv6", "ipv4only", "ipv6only" })
    var ipMode = IPvMode.SYSTEM

    @Option(name = { "--dns" }, description = "DNS (netty, java)", allowedValues = { "java", "netty" })
    var dnsMode = DnsMode.JAVA

    @Option(name = { "--dnsServers" }, description = "Specific DNS Servers (csv, google)")
    var dnsServers: String? = null

    @Option(name = { "--resolve" }, description = "DNS Overrides (HOST:TARGET)")
    var resolve: List<String>? = null

    @Option(name = { "--certificatePin" }, description = "Specific Local Network Interface")
    var certificatePins: List<CertificatePin>? = null

    @Option(name = { "--networkInterface" }, description = "Specific Local Network Interface")
    var networkInterface: String? = null

    @Option(name = { "--clientauth" }, description = "Use Client Authentication (from keystore)")
    var clientAuth = false

    @Option(name = { "--keystore" }, description = "Keystore")
    var keystoreFile: File? = null

    @Option(name = { "--cert" }, description = "Use given server cert (Root CA)")
    var serverCerts = Lists.newArrayList()

    @Option(name = { "--opensc" }, description = "Send OpenSC Client Certificate (slot)")
    var opensc: Integer? = null

    @Option(name = { "--socks" }, description = "Use SOCKS proxy")
    var socksProxy: InetAddressParam? = null

    @Option(name = { "--proxy" }, description = "Use HTTP proxy")
    var proxy: InetAddressParam? = null

    @Option(name = { "--show-credentials" }, description = "Show Credentials")
    var showCredentials = false

    @Option(name = { "--alias-names" }, description = "Show Alias Names")
    var aliasNames = false

    @Option(name = { "-r", "--raw" }, description = "Raw Output")
    var rawOutput = false

    @Option(name = { "-s", "--set" }, description = "Token Set e.g. work")
    var tokenSet: String? = null

    @Option(name = { "--serviceNames" }, description = "Service Names")
    var serviceNames = false

    @Option(name = { "--urlCompletion" }, description = "URL Completion")
    var urlComplete: Boolean = false

    @Option(name = { "--apidoc" }, description = "API Documentation")
    var apiDoc: Boolean = false

    @Option(name = { "--ssldebug" }, description = "SSL Debug")
    var sslDebug: Boolean = false

    @Option(name = { "--user" }, description = "user:password for basic auth")
    var user: String? = null

    @Option(name = { "--maxrequests" }, description = "Concurrency Level")
    private val maxRequests = 16

    var commandName = System.getProperty("command.name", "oksocial")

    var completionFile = System.getenv("COMPLETION_FILE")

    @Arguments(title = "arguments", description = "Remote resource URLs")
    var arguments: List<String> = ArrayList()

    var serviceInterceptor: ServiceInterceptor? = null

    private var authorisation: Authorisation? = null

    var client: OkHttpClient? = null

    var requestBuilder: Request.Builder

    var commandRegistry = CommandRegistry()

    var outputHandler: OutputHandler? = null

    var credentialsStore: CredentialsStore? = null

    var completionVariableCache: CompletionVariableCache? = null

    var locationSource: LocationSource = BestLocation()

    private var eventLoopGroup: NioEventLoopGroup? = null

    private val closeables = Lists.newArrayList()

    private fun versionString(): String {
        return getClass().getPackage().getImplementationVersion()
    }

    fun run(): Int {
        if (sslDebug) {
            System.setProperty("javax.net.debug", "ssl,handshake")
        }

        LoggingUtil.INSTANCE.configureLogging(debug, showHttp2Frames)

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
                PrintCredentials(client, credentialsStore, outputHandler,
                        serviceInterceptor).showCredentials(arguments, ???({ this.createRequestBuilder() }))
                return 0
            }

            if (aliasNames) {
                printAliasNames()
                return 0
            }

            if (serviceNames) {
                outputHandler!!.info(serviceInterceptor!!.names().stream().collect(joining(" ")))
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

            return executeRequests(outputHandler)
        } catch (e: UsageException) {
            outputHandler!!.showError("error: " + e.getMessage(), null)
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
        val docs = ServiceApiDocPresenter(serviceInterceptor, client, credentialsStore)

        fullCompletionUrl.ifPresent({ u ->
            try {
                docs.explainApi(u, outputHandler, client)
            } catch (e: IOException) {
                throw Throwables.propagate(e)
            }
        })
    }

    // TODO refactor this mess out of Main
    @Throws(Exception::class)
    private fun urlCompletionList(): String {
        val command = shellCommand

        val commandCompletor = command.completer()
        if (commandCompletor.isPresent()) {
            val urls = commandCompletion(commandCompletor.get(), arguments)

            val prefix = arguments[arguments.size() - 1]

            if (completionFile != null) {
                urls.toFile(File(completionFile), 0, prefix)
            }

            return urls.getUrls(prefix).stream().collect(joining("\n"))
        }

        val completer = UrlCompleter(serviceInterceptor!!.services(), client, credentialsStore,
                completionVariableCache)

        val fullCompletionUrlOpt = fullCompletionUrl

        // reload hack (in case changed for "" case)
        val originalCompletionUrl = arguments[arguments.size() - 1]

        if (fullCompletionUrlOpt.isPresent()) {
            val fullCompletionUrl = fullCompletionUrlOpt.get()
            val urls = completer.urlList(fullCompletionUrl)

            val strip: Int
            if (!fullCompletionUrl.equals(originalCompletionUrl)) {
                strip = fullCompletionUrl.length() - originalCompletionUrl.length()
            } else {
                strip = 0
            }

            if (completionFile != null) {
                urls.toFile(File(completionFile), strip, originalCompletionUrl)
            }

            return urls.getUrls(fullCompletionUrl).stream()
                    .map({ u -> u.substring(strip) })
                    .collect(joining("\n"))
        } else {
            return ""
        }
    }

    @Throws(IOException::class)
    private fun commandCompletion(urlCompleter: ArgumentCompleter, arguments: List<String>): UrlList {
        return urlCompleter.urlList(arguments[arguments.size() - 1])
    }

    /*
   * The last url in arguments which should be used for completion or apidoc requests.
   * In the case of javascript command expansion, it is expanded first before
   * being returned.
   *
   * n.b. arguments may be modified by this call.
   */
    private // support "" -> http://api.test.com
    val fullCompletionUrl: Optional<String>
        @Throws(Exception::class)
        get() {
            if (arguments.isEmpty()) {
                return empty()
            }

            var urlToComplete = arguments[arguments.size() - 1]

            val command = shellCommand

            if (command is JavascriptApiCommand) {
                val requests = command.buildRequests(client, requestBuilder, arguments)

                if (requests.size() > 0) {
                    val newUrl = requests.get(0).url()
                    if (urlToComplete.isEmpty() && newUrl.encodedPath().equals("/")) {
                        urlToComplete = "/"
                        arguments.remove(arguments.size() - 1)
                        arguments.add(urlToComplete)
                    }

                    val newUrlCompletion = newUrl.toString()

                    if (newUrlCompletion.endsWith(urlToComplete)) {
                        return Optional.of(newUrlCompletion)
                    }
                }
            } else if (UrlCompleter.Companion.isPossibleAddress(urlToComplete)) {
                return Optional.of(urlToComplete)
            }

            return empty()
        }

    @Throws(Exception::class)
    fun initialise() {
        if (outputHandler == null) {
            outputHandler = buildHandler()
        }

        if (credentialsStore == null) {
            credentialsStore = createCredentialsStore()
        }

        closeables.add({
            if (client != null) {
                client!!.dispatcher().executorService().shutdown()
                client!!.connectionPool().evictAll()
            }
        })

        val clientBuilder = createClientBuilder()

        if (user != null) {
            val userParts = user!!.split(":", 2)
            if (userParts.size < 2) {
                throw UsageException("--user should have user:password")
            }
            val credential = Credentials.basic(userParts[0], userParts[1])

            clientBuilder.authenticator({ route, response ->
                logger.fine("Challenges: " + response.challenges())

                // authenticate once
                if (response.request().header("Authorization") != null) {
                    return@clientBuilder.authenticator null
                }

                response.request().newBuilder()
                        .header("Authorization", credential)
                        .build()
            })
        }

        val dispatcher = Dispatcher()
        dispatcher.setMaxRequests(maxRequests)
        dispatcher.setMaxRequestsPerHost(maxRequests)
        clientBuilder.dispatcher(dispatcher)

        val authClient = clientBuilder.build()
        serviceInterceptor = ServiceInterceptor(authClient, credentialsStore)

        authorisation = Authorisation(serviceInterceptor, credentialsStore, authClient, outputHandler)

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

    @Throws(IOException::class)
    private fun applyZipkin(clientBuilder: OkHttpClient.Builder) {
        val config = ZipkinConfig.Companion.load()
        val reporter = config.zipkinSenderUri().map(???({ UriTransportRegistry.Companion.forUri() })).orElse(Platform.get())

        val tracing = Tracing.newBuilder()
                .localServiceName("oksocial")
                .reporter(reporter)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .build()

        val httpTracing = HttpTracing.create(tracing)

        val tracer = tracing.tracer()

        val opener = { tc -> closeables.add({ config.openFunction().apply(tc).ifPresent(???({ this.openLink(it) })) }) }

        clientBuilder.eventListenerFactory(
                { call -> ZipkinTracingListener(call, tracer, httpTracing, opener, zipkinTrace) })

        clientBuilder.addNetworkInterceptor(ZipkinTracingInterceptor(tracing))

        closeables.add({
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
            return FixedTokenCredentialsStore(token)
        }

        return if (PlatformUtil.isOSX()) {
            OSXCredentialsStore(ofNullable(tokenSet))
        } else {
            PreferencesCredentialsStore(ofNullable(tokenSet))
        }
    }

    private fun closeClients() {
        for (c in closeables) {
            IOUtils.closeQuietly(c)
        }
    }

    private fun buildHandler(): OutputHandler {
        val responseExtractor = OkHttpResponseExtractor()

        return if (outputDirectory != null) {
            DownloadHandler(responseExtractor, outputDirectory)
        } else if (rawOutput) {
            DownloadHandler(responseExtractor, File("-"))
        } else {
            ConsoleHandler.instance(responseExtractor)
        }
    }

    @Throws(Exception::class)
    private fun executeRequests(outputHandler: OutputHandler): Int {
        val command = shellCommand

        val requests = command.buildRequests(client, requestBuilder, arguments)

        if (!command.handlesRequests()) {
            if (requests.isEmpty()) {
                throw UsageException("no urls specified")
            }

            val responseFutures = enqueueRequests(requests, client)
            val failed = processResponses(outputHandler, responseFutures)
            return if (failed) -5 else 0
        }

        return 0
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun processResponses(outputHandler: OutputHandler,
                                 responseFutures: List<Future<Response>>): Boolean {
        var failed = false
        for (responseFuture in responseFutures) {
            if (failed) {
                responseFuture.cancel(true)
            } else {
                try {
                    responseFuture.get().use({ response -> showOutput(outputHandler, response) })
                } catch (ee: ExecutionException) {
                    outputHandler.showError("request failed", ee.getCause())
                    failed = true
                }

            }
        }
        return failed
    }

    @Throws(IOException::class)
    private fun showOutput(outputHandler: OutputHandler, response: Response) {
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
        } else if (!response.isSuccessful()) {
            outputHandler.showError(StatusLine.get(response).toString(), null)
        }

        outputHandler.showOutput(response)
    }

    private fun enqueueRequests(requests: List<Request>, client: OkHttpClient?): List<Future<Response>> {
        val responseFutures = Lists.newArrayList()

        for (request in requests) {
            logger.log(Level.FINE, "url " + request.url())

            if (requests.size() > 1 && !debug) {
                System.err.println(request.url())
            }

            responseFutures.add(makeRequest(client, request))
        }
        return responseFutures
    }

    private val shellCommand: ShellCommand
        get() {
            val shellCommand = commandRegistry.getCommandByName(commandName).orElse(OksocialCommand())

            if (shellCommand is MainAware) {
                (shellCommand as MainAware).setMain(this)
            }

            return shellCommand
        }

    private fun printAliasNames() {
        val names = Sets.newTreeSet(commandRegistry.names())

        names.forEach(???({ outputHandler!!.info() }))
    }

    private fun makeRequest(client: OkHttpClient?, request: Request): Future<Response> {
        logger.log(Level.FINE, "Request " + request)

        val call = client!!.newCall(request)

        val result = OkHttpResponseFuture()

        call.enqueue(result)

        return result.getFuture()
    }

    @Throws(Exception::class)
    private fun authorize() {
        val auth = findAuthInterceptor()

        authorisation!!.authorize(auth, ofNullable(token), arguments)
    }

    @Throws(Exception::class)
    private fun renew() {
        val auth = findAuthInterceptor()

        authorisation!!.renew(auth)
    }

    @Throws(Exception::class)
    private fun findAuthInterceptor(): Optional<AuthInterceptor<*>> {
        val command = shellCommand

        var auth = command.authenticator().flatMap({ authName -> serviceInterceptor!!.getByName(authName) })

        if (!auth.isPresent() && !arguments.isEmpty()) {
            val name = arguments.remove(0)

            auth = serviceInterceptor!!.findAuthInterceptor(name)
        }
        return auth
    }

    @Throws(Exception::class)
    fun createClientBuilder(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        builder.followSslRedirects(!dontFollowRedirects)
        builder.followRedirects(!dontFollowRedirects)
        if (connectTimeout != null) {
            builder.connectTimeout(connectTimeout, SECONDS)
        }
        if (readTimeout != null) {
            builder.readTimeout(readTimeout, SECONDS)
        }

        builder.dns(buildDns())

        if (networkInterface != null) {
            builder.socketFactory(socketFactory)
        }

        configureTls(builder)

        if (cacheDirectory != null) {
            builder.cache(Cache(cacheDirectory, 64 * 1024 * 1024))
        }

        // TODO move behind AuthInterceptor API
        builder.addNetworkInterceptor(TwitterCachingInterceptor())
        builder.addNetworkInterceptor(TwitterDeflatedResponseInterceptor())

        if (curl) {
            builder.addNetworkInterceptor(CurlInterceptor(???({ System.err.println() })))
        }

        if (debug) {
            val loggingInterceptor = HttpLoggingInterceptor(???({ logger.info() }))
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
            builder.networkInterceptors().add(loggingInterceptor)
        }

        if (socksProxy != null) {
            builder.proxy(Proxy(Proxy.Type.SOCKS, socksProxy!!.getAddress()))
        } else if (proxy != null) {
            builder.proxy(Proxy(Proxy.Type.HTTP, proxy!!.getAddress()))
        }

        if (protocols != null) {
            builder.protocols(ProtocolUtil.INSTANCE.parseProtocolList(protocols))
        }

        return builder
    }

    private fun buildDns(): Dns {
        var dns: Dns
        if (dnsMode === DnsMode.NETTY) {
            dns = NettyDns.Companion.byName(ipMode, getEventLoopGroup(), dnsServers)
        } else if (dnsMode === DnsMode.DNSGOOGLE) {
            dns = DnsSelector(ipMode,
                    GoogleDns.Companion.fromHosts({ this@Main.client }, ipMode, "216.58.216.142", "216.239.34.10",
                            "2607:f8b0:400a:809::200e"))
        } else {
            if (dnsServers != null) {
                throw UsageException("unable to set dns servers with java DNS")
            }

            dns = DnsSelector(ipMode, Dns.SYSTEM)
        }
        if (resolve != null) {
            dns = DnsOverride.Companion.build(dns, resolve)
        }
        return dns
    }

    private fun getEventLoopGroup(): NioEventLoopGroup {
        if (eventLoopGroup == null) {
            val threadFactory = DefaultThreadFactory("netty", true)
            eventLoopGroup = NioEventLoopGroup(5, threadFactory)

            closeables.add({ eventLoopGroup!!.shutdownGracefully(0, 0, TimeUnit.SECONDS) })
        }

        return eventLoopGroup
    }

    private val socketFactory: SocketFactory
        @Throws(SocketException::class)
        get() {
            val socketFactory = InterfaceSocketFactory.Companion.byName(networkInterface)

            if (!socketFactory.isPresent()) {
                throw UsageException("networkInterface '$networkInterface' not found")
            }

            return socketFactory.get()
        }

    @Throws(Exception::class)
    private fun configureTls(builder: OkHttpClient.Builder) {
        val callbackHandler = ConsoleCallbackHandler()

        // possibly null
        var keystore: KeyStore? = null

        if (keystoreFile != null) {
            keystore = getKeyStore(keystoreFile)
        }

        val keyManagers = Lists.newArrayList()

        if (opensc != null) {
            keyManagers.addAll(asList(OpenSCUtil.INSTANCE.getKeyManagers(callbackHandler, opensc)))
        } else if (clientAuth) {
            if (keystore == null) {
                throw UsageException("--clientauth specified without --keystore")
            }

            keyManagers.add(createKeyManager(keystore, callbackHandler))
        }

        val trustManager: X509TrustManager
        if (allowInsecure) {
            trustManager = InsecureTrustManager()
            builder.hostnameVerifier(InsecureHostnameVerifier())
        } else {
            val trustManagers = Lists.newArrayList()

            if (keystore != null) {
                trustManagers.add(trustManagerForKeyStore(keystore))
            }

            if (!serverCerts.isEmpty()) {
                trustManagers.add(CertificateUtils.INSTANCE.load(serverCerts))
            }

            trustManager = CertificateUtils.INSTANCE.combineTrustManagers(trustManagers)
        }

        builder.sslSocketFactory(createSslSocketFactory(keyManagerArray(keyManagers), trustManager),
                trustManager)

        if (certificatePins != null) {
            builder.certificatePinner(CertificatePin.Companion.buildFromCommandLine(certificatePins))
        }
    }

    private val requestMethod: String
        get() {
            if (method != null) {
                return method
            }
            return if (data != null) {
                "POST"
            } else "GET"
        }

    private fun getRequestBody(headerMap: Map<String, String>): RequestBody? {
        if (data == null) {
            return null
        }

        var mimeType = "application/x-www-form-urlencoded"

        for (k in headerMap.keySet()) {
            if ("Content-Type".equalsIgnoreCase(k)) {
                mimeType = headerMap.remove(k)
                break
            }
        }

        try {
            return RequestBody.create(MediaType.parse(mimeType), FileContent.INSTANCE.readParamBytes(data))
        } catch (e: IOException) {
            throw UsageException(e.getMessage())
        }

    }

    fun createRequestBuilder(): Request.Builder {
        val requestBuilder = Request.Builder()

        val headerMap = headerMap(headers)

        requestBuilder.method(requestMethod, getRequestBody(headerMap))

        if (headers != null) {
            headerMap.forEach({ k, v -> requestBuilder.header(k, v) })
        }
        if (referer != null) {
            requestBuilder.header("Referer", referer)
        }
        requestBuilder.header("User-Agent", userAgent)

        return requestBuilder
    }

    companion object {
        private val logger = Logger.getLogger(Main::class.java!!.getName())

        internal val NAME = "oksocial"

        private fun fromArgs(vararg args: String): Main {
            return SingleCommand.singleCommand(Main::class.java).parse(args)
        }

        fun main(vararg args: String) {
            val result = fromArgs(*args).run()
            System.exit(result)
        }
    }
}
