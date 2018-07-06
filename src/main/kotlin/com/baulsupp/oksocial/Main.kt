package com.baulsupp.oksocial

import com.baulsupp.oksocial.Main.Companion.NAME
import com.baulsupp.oksocial.apidocs.ServiceApiDocPresenter
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.PrintCredentials
import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.commands.CommandRegistry
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.OkApiCommand
import com.baulsupp.oksocial.commands.OksocialCommand
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.commands.listOptions
import com.baulsupp.oksocial.completion.CompletionCommand
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.DirCompletionVariableCache
import com.baulsupp.oksocial.completion.UrlCompleter
import com.baulsupp.oksocial.credentials.DefaultToken
import com.baulsupp.oksocial.credentials.FixedTokenCredentialsStore
import com.baulsupp.oksocial.kotlin.execute
import com.baulsupp.oksocial.okhttp.FailedResponse
import com.baulsupp.oksocial.okhttp.OkHttpResponseExtractor
import com.baulsupp.oksocial.okhttp.PotentialResponse
import com.baulsupp.oksocial.okhttp.SuccessfulResponse
import com.baulsupp.oksocial.output.DownloadHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.sse.SseOutput
import com.baulsupp.oksocial.sse.handleSseResponse
import com.baulsupp.oksocial.util.FileContent
import com.baulsupp.oksocial.util.HeaderUtil
import io.airlift.airline.Command
import io.airlift.airline.Option
import io.airlift.airline.ParseOptionConversionException
import io.airlift.airline.ParseOptionMissingValueException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.StatusLine
import okhttp3.internal.platform.Platform
import org.conscrypt.OpenSSLProvider
import java.io.File
import java.io.IOException
import java.security.Security
import java.util.logging.Level
import java.util.logging.Logger

@Command(name = NAME, description = "A curl for social apis.")
class Main : CommandLineClient() {
  private val logger = Logger.getLogger(Main::class.java.name)

  @Option(name = ["-X", "--request"], description = "Specify request command to use")
  var method: String? = null

  @Option(name = ["-d", "--data"], description = "HTTP POST data")
  var data: String? = null

  @Option(name = ["-H", "--header"], description = "Custom header to pass to server")
  var headers: java.util.List<String>? = null

  @Option(name = ["--noFollow"], description = "Follow redirects")
  var dontFollowRedirects = false

  @Option(name = ["--referer"], description = "Referer URL")
  var referer: String? = null

  @Option(name = ["-o", "--output"], description = "Output file/directory")
  var outputDirectory: File? = null

  @Option(name = ["--authorize"], description = "Authorize API")
  var authorize: Boolean = false

  @Option(name = ["--renew"], description = "Renew API Authorization")
  var renew: Boolean = false

  @Option(name = ["--remove"], description = "Remove API Authorization")
  var remove: Boolean = false

  @Option(name = ["--token"], description = "Use existing Token for authorization")
  var token: String? = null

  @Option(name = ["--showCredentials"], description = "Show Credentials")
  var showCredentials = false

  @Option(name = ["--complete"], description = "Complete options")
  var complete: String? = null

  @Option(name = ["--urlCompletion"], description = "URL Completion")
  var urlComplete: Boolean = false

  @Option(name = ["--apidoc"], description = "API Documentation")
  var apiDoc: Boolean = false

  var commandName = Main.command

  var completionFile: File? = System.getenv("COMPLETION_FILE")?.let { File(it) }

  var commandRegistry = CommandRegistry()

  lateinit var completionVariableCache: CompletionVariableCache

  override fun runCommand(runArguments: List<String>): Int {
    runBlocking {
      when {
        showCredentials -> PrintCredentials(this@Main).showCredentials(arguments)
        complete != null -> completeOption()
        urlComplete -> CompletionCommand(this@Main).complete()
        apiDoc -> showApiDocs()
        authorize -> authorize()
        renew -> renew()
        remove -> remove()
        else -> executeRequests(outputHandler)
      }
    }

    return 0
  }

  private suspend fun Main.completeOption() {
    return outputHandler.info(listOptions(complete!!).toSortedSet().joinToString(" "))
  }

  override fun createClientBuilder(): OkHttpClient.Builder {
    val builder = super.createClientBuilder()

    builder.followSslRedirects(!dontFollowRedirects)
    builder.followRedirects(!dontFollowRedirects)

    return builder
  }

  suspend fun showApiDocs() {
    getFullCompletionUrl()?.let { u ->
      ServiceApiDocPresenter(authenticatingInterceptor).explainApi(u, outputHandler, client, token())
    }
  }

  suspend fun applyRequestFields(request: Request): Request {
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

    requestBuilder.tag(this.token())

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

    var urlToComplete = arguments[arguments.size - 1]

    val command = getShellCommand()

    if (command is OkApiCommand) {
      val requests = command.buildRequests(client, arguments)

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

  override fun initialise() {
    if (token != null && !authorize) {
      credentialsStore = FixedTokenCredentialsStore(token!!)
    }

    super.initialise()

    if (!this::completionVariableCache.isInitialized) {
      completionVariableCache = DirCompletionVariableCache.TEMP
    }
  }

  suspend fun executeRequests(outputHandler: OutputHandler<Response>): Int {
    val command = getShellCommand()

    val requests = command.buildRequests(client, arguments)

    if (!command.handlesRequests()) {
      if (requests.isEmpty()) {
        throw UsageException("no urls specified")
      }

      val responses = requests.map {
        async(CommonPool) { submitRequest(it) }
      }
      val failed = processResponses(outputHandler, responses)
      return if (failed) -5 else 0
    }

    return 0
  }

  suspend fun processResponses(
    outputHandler: OutputHandler<Response>,
    responses: List<Deferred<PotentialResponse>>
  ): Boolean {
    var failed = false
    for (deferredResponse in responses) {
      val response = deferredResponse.await()
      when (response) {
        is SuccessfulResponse -> {
          showOutput(outputHandler, response)
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

  suspend fun showOutput(outputHandler: OutputHandler<Response>, wrappedResponse: SuccessfulResponse) {
    val response = wrappedResponse.response
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("OkHttp Platform: ${Platform.get().javaClass.simpleName}")
      logger.fine("TLS Version: ${response.handshake().tlsVersion()}")
      logger.fine("Protocol: ${response.protocol()}")
      logger.fine("Cipher: ${response.handshake().cipherSuite()}")
      logger.fine("Peer Principal: ${response.handshake().peerPrincipal()}")
    }

    if (showHeaders) {
      outputHandler.info(StatusLine.get(response).toString())
      val headers = response.headers()
      var i = 0
      val size = headers.size()
      while (i < size) {
        outputHandler.info("${headers.name(i)}: ${headers.value(i)}")
        i++
      }
      outputHandler.info("")
    } else if (!response.isSuccessful) {
      outputHandler.showError(StatusLine.get(response).toString(), null)
    }

    if (isEventStream(response.body()?.contentType())) {
      response.handleSseResponse(SseOutput(outputHandler))
    } else {
      outputHandler.showOutput(response)
    }
  }

  private fun isEventStream(contentType: MediaType?): Boolean {
    return contentType != null && contentType.type() == "text" && contentType.subtype() == "event-stream"
  }

  suspend fun submitRequest(request: Request): PotentialResponse {
    val finalRequest = applyRequestFields(request)

    logger.log(Level.FINE, "url " + finalRequest.url())
    logger.log(Level.FINE, "Request $finalRequest")

    return try {
      val response = client.execute(finalRequest)
      SuccessfulResponse(response)
    } catch (ioe: IOException) {
      FailedResponse(ioe)
    }
  }

  fun getShellCommand(): ShellCommand {
    var shellCommand = commandRegistry.getCommandByName(commandName)

    if (shellCommand == null) {
      shellCommand = OksocialCommand()
    }

    if (shellCommand is MainAware) {
      (shellCommand as MainAware).setMain(this)
    }

    return shellCommand
  }

  suspend fun authorize() {
    authorisation.authorize(findAuthInterceptor(), token, arguments, tokenSet ?: DefaultToken.name)
  }

  suspend fun renew() {
    authorisation.renew(findAuthInterceptor(), tokenSet ?: DefaultToken.name)
  }

  suspend fun remove() {
    authorisation.remove(findAuthInterceptor(), tokenSet ?: DefaultToken.name)
  }

  private fun findAuthInterceptor(): AuthInterceptor<*>? {
    val command = getShellCommand()

    val authenticator = command.authenticator()
    var auth: AuthInterceptor<*>? = null

    if (authenticator != null) {
      auth = authenticatingInterceptor.getByName(authenticator)
    }

    if (auth == null && !arguments.isEmpty()) {
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
    outputDirectory != null -> DownloadHandler(OkHttpResponseExtractor(), outputDirectory!!)
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

      RequestBody.create(MediaType.get(mimeType), content)
    } catch (e: IOException) {
      throw UsageException(e.message!!)
    }
  }

  private fun predictContentType(content: ByteArray): String = if (content.isNotEmpty() && content[0] == '{'.toByte()) {
    "application/json"
  } else {
    "application/x-www-form-urlencoded"
  }

  companion object {
    const val NAME = "oksocial"
    val command = System.getProperty("command.name", "oksocial")!!

    @JvmStatic
    fun main(vararg args: String) = runBlocking {
      try {
        Security.insertProviderAt(OpenSSLProvider(), 1)
      } catch (e: NoClassDefFoundError) {
        // Drop back to JDK
      }
      try {
        val result = CommandLineClient.fromArgs<Main>(*args).run()
        System.exit(result)
      } catch (e: ParseOptionMissingValueException) {
        System.err.println("$command: ${e.message}")
        System.exit(-1)
      } catch (e: ParseOptionConversionException) {
        System.err.println("$command: ${e.message}")
        System.exit(-1)
      } catch (e: UsageException) {
        System.err.println("${com.baulsupp.oksocial.Main.command}: ${e.message}")
        System.exit(-1)
      } catch (e: Throwable) {
        e.printStackTrace()
        System.exit(-1)
      }
    }
  }
}
