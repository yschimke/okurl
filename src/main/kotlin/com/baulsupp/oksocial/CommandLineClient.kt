package com.baulsupp.oksocial

import com.baulsupp.oksocial.authenticator.Authorisation
import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.location.BestLocation
import com.baulsupp.oksocial.location.LocationSource
import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.IPvMode
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.security.CertificatePin
import com.baulsupp.oksocial.util.InetAddressParam
import io.airlift.airline.Arguments
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.netty.channel.nio.NioEventLoopGroup
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.Closeable
import java.io.File
import java.util.ArrayList

open class CommandLineClient: HelpOption() {

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

  @Arguments(title = "arguments", description = "Remote resource URLs")
  var arguments: MutableList<String> = ArrayList()

  var serviceInterceptor: ServiceInterceptor? = null

  var authorisation: Authorisation? = null

  var client: OkHttpClient? = null

  var outputHandler: OutputHandler<Response>? = null

  var credentialsStore: CredentialsStore? = null

  var locationSource: LocationSource = BestLocation()

  var eventLoopGroup: NioEventLoopGroup? = null

  val closeables = mutableListOf<Closeable>()

  fun versionString(): String {
    return this.javaClass.`package`.implementationVersion ?: "dev"
  }

}