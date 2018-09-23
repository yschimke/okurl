package com.baulsupp.okurl.authenticator

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.util.logging.Level
import java.util.logging.Logger

class SimpleWebServer(
  private val codeReader: (HttpUrl) -> String?,
  private val port: Int = 3000
) : Closeable, HttpHandler {
  private val logger = Logger.getLogger(SimpleWebServer::class.java.name)

  private var server: HttpServer = HttpServer.create(InetSocketAddress("localhost", port), 1)
  private val channel = Channel<Result<String>>()

  val redirectUri = "http://localhost:$port/callback"

  init {
    server.createContext("/", this)
    server.start()

    logger.log(Level.FINE, "listening at $redirectUri")
  }

  override fun handle(exchange: HttpExchange) {
    exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
    exchange.sendResponseHeaders(200, 0)

    PrintWriter(exchange.responseBody).use { out ->
      processRequest(exchange, out)
    }

    exchange.close()
  }

  fun processRequest(exchange: HttpExchange, out: PrintWriter) {
    val result = runCatching {
      val url = HttpUrl.get("http://localhost:$port${exchange.requestURI}")

      val error = url.queryParameter("error")

      if (error != null) {
        throw IOException(error)
      }

      codeReader(url) ?: throw IllegalArgumentException("no code read")
    }.onSuccess {
      out.println(generateSuccessBody())
    }.onFailure {
      out.println(generateFailBody("$it"))
    }

    this.channel.offer(result) || throw IllegalStateException("unable to send to channel")
  }

  private fun generateSuccessBody(): String = """<html>
<body background="http://win.blogadda.com/wp-content/uploads/2015/08/inspire-win-15.jpg">
<h1>Authorization Token Received!</h1>
</body>
</html>"""

  private fun generateFailBody(error: String): String = """<html>
<body background="http://adsoftheworld.com/sites/default/files/fail_moon_aotw.jpg">
<h1>Authorization Error!</h1>
<p style="font-size: 600%; font-family: Comic Sans, Comic Sans MS, cursive;">$error</p></body>
</html>"""

  override fun close() {
    channel.close()
    server.stop(0)
  }

  suspend fun waitForCode(): String = channel.receive().getOrThrow()

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
          ws.waitForCode()
        }
        println("result = $s")
      }
    }
  }
}
