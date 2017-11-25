package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.kotlin.await
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class SimpleWebServer(private val codeReader: (HttpUrl) -> String?,
        private val port: Int = 3000) : Closeable, HttpHandler {
  private val logger = Logger.getLogger(SimpleWebServer::class.java.name)

  private var server: HttpServer
  var f: CompletableFuture<String> = CompletableFuture()

  init {
    server = HttpServer.create(InetSocketAddress("localhost", port), 1)
    server.createContext("/", this)
    server.start()

    logger.log(Level.FINE, "listening at $redirectUri")
  }

  val redirectUri: String
    get() = "http://localhost:$port/callback"

  override fun handle(exchange: HttpExchange) {
    val url = HttpUrl.parse("http://localhost:${port}${exchange.requestURI}")!!

    exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
    exchange.sendResponseHeaders(200, 0)
    PrintWriter(exchange.responseBody).use { out ->
      val error = url.queryParameter("error")

      try {
        if (error != null) {
          IOException(error)
        }

        val code = codeReader(url)

        if (code == null) {
          throw IllegalArgumentException("no code read")
        }

        out.println(generateSuccessBody())
        out.flush()
        exchange.close()
        f.complete(code)
      } catch (e: Exception) {
        out.println(generateFailBody(url, e.toString()))
        out.flush()
        exchange.close()
        f.completeExceptionally(e)
      }
    }
  }

  private fun generateSuccessBody(): String {

    return """<html>
<body background="http://win.blogadda.com/wp-content/uploads/2015/08/inspire-win-15.jpg">
<h1>Authorization Token Received!</h1>
</body>
</html>"""
  }

  private fun generateFailBody(url: HttpUrl, error: String): String {
//        val params = url.param.joinToString("<br/>") { e -> e.key + " = " + e.value.joinToString(", ") }

    return """<html>
<body background="http://adsoftheworld.com/sites/default/files/fail_moon_aotw.jpg">
<h1>Authorization Error!</h1>
<p style="font-size: 600%; font-family: Comic Sans, Comic Sans MS, cursive;">$error</p></body>
</html>"""
  }

  override fun close() {
    server.stop(0)
  }

  suspend fun waitForCodeAsync(): String = f.await()

  fun waitForCode(): String = runBlocking { waitForCodeAsync() }

  companion object {
    fun forCode(): SimpleWebServer {
      return SimpleWebServer({ r ->
        r.queryParameter("code")
      })
    }

    @JvmStatic
    fun main(args: Array<String>) {
      SimpleWebServer.forCode().use { ws ->
        val s = runBlocking {
          ws.waitForCodeAsync()
        }
        println("result = $s")
      }
    }
  }
}
