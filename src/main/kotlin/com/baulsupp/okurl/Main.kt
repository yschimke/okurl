package com.baulsupp.okurl

import com.baulsupp.okurl.Main.Companion.NAME
import com.baulsupp.okurl.apidocs.ServiceApiDocPresenter
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.PrintCredentials
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.commands.listOptions
import com.baulsupp.okurl.completion.CompletionCommand
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.DirCompletionVariableCache
import com.baulsupp.okurl.completion.UrlCompleter
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.FixedTokenCredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.execute
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.okhttp.FailedResponse
import com.baulsupp.okurl.okhttp.OkHttpResponseExtractor
import com.baulsupp.okurl.okhttp.PotentialResponse
import com.baulsupp.okurl.okhttp.SuccessfulResponse
import com.baulsupp.okurl.okhttp.WireSharkListenerFactory
import com.baulsupp.okurl.sse.SseOutput
import com.baulsupp.okurl.sse.handleSseResponse
import com.baulsupp.okurl.util.ClientException
import com.baulsupp.okurl.util.FileContent
import com.baulsupp.okurl.util.HeaderUtil
import com.baulsupp.schoutput.UsageException
import com.baulsupp.schoutput.handler.DownloadHandler
import com.baulsupp.schoutput.handler.OutputHandler
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import okhttp3.Handshake
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http.StatusLine
import okhttp3.internal.platform.Platform
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.conscrypt.Conscrypt
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IVersionProvider
import picocli.CommandLine.Option
import java.io.File
import java.io.IOException
import java.security.Security
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

@Command(
  name = NAME, description = ["A curl for social apis."],
  mixinStandardHelpOptions = true, versionProvider = Main.Companion.VersionProvider::class
)
class Main : CommandLineClient() {
  private val logger = Logger.getLogger(Main::class.java.name)

  @Option(names = ["-X", "--request"], description = ["Specify request command to use"])
  var method: String? = null

  @Option(names = ["-d", "--data"], description = ["HTTP POST data"])
  var data: String? = null

  @Option(names = ["-H", "--header"], description = ["Custom header to pass to server"])
  var headers: MutableList<String>? = null

  @Option(names = ["--noFollow"], description = ["Follow redirects"])
  var dontFollowRedirects = false

  @Option(names = ["--referer"], description = ["Referer URL"])
  var referer: String? = null

  @Option(names = ["-o", "--output"], description = ["Output file/directory"])
  var outputDirectory: File? = null

  @Option(names = ["--authorize"], description = ["Authorize API"])
  var authorize: String? = null

  @Option(names = ["--renew"], description = ["Renew API Authorization"])
  var renew: Boolean = false

  @Option(names = ["--remove"], description = ["Remove API Authorization"])
  var remove: Boolean = false

  @Option(names = ["--conscrypt"], description = ["Use Conscrypt"])
  var conscrypt: Boolean = false

  @Option(names = ["--token"], description = ["Use existing Token for authorization"])
  var token: String? = null

  @Option(names = ["--showCredentials"], description = ["Show Credentials"])
  var showCredentials = false

  @Option(names = ["--complete"], description = ["Complete options"])
  var complete: String? = null

  @Option(names = ["--urlCompletion"], description = ["URL Completion"])
  var urlComplete: Boolean = false

  @Option(names = ["--apidoc"], description = ["API Documentation"])
  var apiDoc: Boolean = false

  var commandName = command

  lateinit var completionVariableCache: CompletionVariableCache

  override fun runCommand(runArguments: List<String>): Int {
    runBlocking {
      when {
        showCredentials -> PrintCredentials(this@Main).showCredentials(arguments)
        complete != null -> completeOption()
        urlComplete -> CompletionCommand(this@Main).complete()
        apiDoc -> showApiDocs()
        authorize != null -> authorize()
        renew -> renew()
        remove -> remove()
        else -> executeRequests(outputHandler)
      }
    }

    return 0
  }

  private suspend fun completeOption() {
    return outputHandler.info(
      listOptions(complete!!).toSortedSet()
        .joinToString(" ")
    )
  }

  override fun createClientBuilder(): OkHttpClient.Builder {
    val builder = super.createClientBuilder()

    builder.followSslRedirects(!dontFollowRedirects)
    builder.followRedirects(!dontFollowRedirects)

    return builder
  }

  suspend fun showApiDocs() {
    getFullCompletionUrl()?.let { u ->
      ServiceApiDocPresenter(authenticatingInterceptor).explainApi(
        u, outputHandler, client,
        token()
      )
    }
  }

  suspend fun applyRequestFields(request: Request): Request {
    val requestBuilder = request.newBuilder()

    val headerMap = HeaderUtil.headerMap(headers?.toList())
      .toMutableMap()

    requestBuilder.method(getRequestMethod(), getRequestBody(headerMap))

    if (headers != null) {
      headerMap.forEach { (k, v) -> requestBuilder.header(k, v) }
    }
    if (referer != null) {
      requestBuilder.header("Referer", referer!!)
    }
    requestBuilder.header("User-Agent", userAgent)

    requestBuilder.tag(Token::class.java, this.token())

    return requestBuilder.build()
  }

  /*
   * The last url in arguments which should be used for completion or apidoc requests.
   * In the case of javascript command expansion, it is expanded first before
   * being returned.
   *
   * n.b. arguments may be modified by this call.
   */
  fun getFullCompletionUrl(): String? {
    if (arguments.isEmpty()) {
      return null
    }

    val urlToComplete = arguments[arguments.size - 1]

    if (UrlCompleter.isPossibleAddress(urlToComplete)) {
      return urlToComplete
    }

    return null
  }

  override fun initialise() {
    if (conscrypt) {
      setupProvider()
    }

    if (token != null && authorize == null) {
      credentialsStore = FixedTokenCredentialsStore(token!!)
    }

    super.initialise()

    if (!this::completionVariableCache.isInitialized) {
      completionVariableCache = DirCompletionVariableCache.TEMP
    }
  }

  suspend fun executeRequests(outputHandler: OutputHandler<Response>): Int = supervisorScope {
    val requests = buildRequests(arguments)

    if (requests.isEmpty()) {
      throw UsageException("no urls specified")
    }

    val responses = requests.map { async { submitRequest(it) } }
    val failed = processResponses(outputHandler, responses)
    if (failed) -5 else 0
  }

  private suspend fun processResponses(
    outputHandler: OutputHandler<Response>,
    responses: List<Deferred<PotentialResponse>>
  ): Boolean {
    var failed = false
    for (deferredResponse in responses) {
      when (val response = deferredResponse.await()) {
        is SuccessfulResponse -> {
          showOutput(outputHandler, response)
          response.response.close()
        }
        is FailedResponse -> {
          if (response.exception is ClientException) {
            outputHandler.showError(response.exception.responseMessage)
          } else {
            outputHandler.showError("request failed", response.exception)
          }
          failed = true
        }
      }
    }
    return failed
  }

  suspend fun showOutput(
    outputHandler: OutputHandler<Response>,
    wrappedResponse: SuccessfulResponse
  ) {
    val response = wrappedResponse.response
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("OkHttp Platform: ${Platform.get().javaClass.simpleName}")
      val handshake: Handshake? = response.handshake
      logger.fine("Protocol: ${response.protocol}")

      if (handshake != null) {
        logger.fine("TLS Version: ${handshake.tlsVersion}")
        logger.fine("Cipher: ${handshake.cipherSuite}")
        logger.fine("Peer Principal: ${handshake.peerPrincipal ?: "none"}")
        logger.fine("Local Principal: ${handshake.localPrincipal ?: "none"}")
        logger.fine("JVM: ${System.getProperty("java.vm.version")}")
      }
    }

    if (showHeaders) {
      outputHandler.info(
        StatusLine.get(response)
          .toString()
      )
      val headers = response.headers
      var i = 0
      val size = headers.size
      while (i < size) {
        outputHandler.info("${headers.name(i)}: ${headers.value(i)}")
        i++
      }
      outputHandler.info("")
    } else if (!response.isSuccessful) {
      outputHandler.showError(
        StatusLine.get(response)
          .toString(), null
      )
    }

    if (isEventStream(response.body.contentType())) {
      response.handleSseResponse(SseOutput(outputHandler))
    } else {
      outputHandler.showOutput(response)
    }
  }

  private fun isEventStream(contentType: MediaType?): Boolean {
    return contentType != null && contentType.type == "text" && contentType.subtype == "event-stream"
  }

  suspend fun submitRequest(request: Request): PotentialResponse {
    val finalRequest = applyRequestFields(request)

    logger.log(Level.FINE, "url " + finalRequest.url)
    logger.log(Level.FINE, "Request $finalRequest")

    return try {
      val response = client.execute(finalRequest)
      SuccessfulResponse(response)
    } catch (ioe: IOException) {
      FailedResponse(ioe)
    }
  }

  fun buildRequests(
    arguments: List<String>
  ): List<Request> {
    return try {
      arguments.map { u -> request(u) }
    } catch (iae: IllegalArgumentException) {
      throw UsageException(iae.message.orEmpty())
    }
  }

  suspend fun authorize() {
    authorisation.authorize(
      findAuthInterceptor(), authorize!!, token, arguments, tokenSet ?: DefaultToken.name
    )
  }

  suspend fun renew() {
    authorisation.renew(findAuthInterceptor(), tokenSet ?: DefaultToken.name)
  }

  suspend fun remove() {
    authorisation.remove(findAuthInterceptor(), tokenSet ?: DefaultToken.name)
  }

  private fun findAuthInterceptor(): AuthInterceptor<*>? {
    var auth: AuthInterceptor<*>? = null

    if (authorize != null) {
      auth = authenticatingInterceptor.getByName(authorize!!)
    } else if (auth == null && arguments.isNotEmpty()) {
      val name = arguments.removeAt(0)

      auth = authenticatingInterceptor.findAuthInterceptor(name)
    }
    return auth
  }

  private fun getRequestMethod(): String = when {
    method != null -> method!!
    data != null -> "POST"
    else -> "GET"
  }

  override fun buildHandler(): OutputHandler<Response> = when {
    outputDirectory != null -> DownloadHandler(OkHttpResponseExtractor(), FileSystem.SYSTEM, outputDirectory!!.toOkioPath())
    else -> super.buildHandler()
  }

  private suspend fun getRequestBody(headerMap: MutableMap<String, String>): RequestBody? {
    if (data == null) {
      return null
    }

    return try {
      val content = FileContent.readParamBytes(data!!)

      val mimeType = headerMap.keys
        .firstOrNull { "Content-Type".equals(it, ignoreCase = true) }
        ?.let { headerMap.remove(it)!! }
        ?: predictContentType(content)

      content.toRequestBody(mimeType.toMediaType(), 0, content.size)
    } catch (e: IOException) {
      throw UsageException(e.message!!)
    }
  }

  private fun predictContentType(content: ByteArray): String =
    if (content.isNotEmpty() && content[0] == '{'.code.toByte()) {
      "application/json"
    } else {
      "application/x-www-form-urlencoded"
    }

  override fun name(): String = NAME

  companion object {
    lateinit var moshi: Moshi
    lateinit var client: OkHttpClient

    const val NAME = "okurl"
    val command = System.getProperty("command.name", "okurl")!!

    fun setupProvider() {
      // Prefer Conscrypt over JDK 11
      try {
        Security.insertProviderAt(
          Conscrypt.newProviderBuilder()
            .provideTrustManager(true)
            .build(),
          1
        )
      } catch (e: NoClassDefFoundError) {
        // Drop back to JDK
      }
    }

    class VersionProvider : IVersionProvider {
      override fun getVersion(): Array<String> {
        return arrayOf(
          "${NAME} ${versionString()}",
          "Protocols: ${
            Protocol.values()
              .joinToString(", ")
          }",
          "Platform: ${Platform.get()::class.java.simpleName}"
        )
      }
    }
  }
}

fun main(args: Array<String>) {
  WireSharkListenerFactory.register()

  exitProcess(CommandLine(Main()).execute(*args))
}
