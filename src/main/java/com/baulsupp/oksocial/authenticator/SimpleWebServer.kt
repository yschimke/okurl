package com.baulsupp.oksocial.authenticator

import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.util.Arrays
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Function
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler

import java.util.stream.Collectors.joining

class SimpleWebServer<T> @Throws(IOException::class)
constructor(private val codeReader: Function<HttpServletRequest, T>) : AbstractHandler(), Closeable {
    private val port = 3000
    private val f = CompletableFuture<T>()
    private val server: Server

    init {
        org.eclipse.jetty.util.log.Log.initialized()
        org.eclipse.jetty.util.log.Log.setLog(NullLogger())

        server = Server(port)
        try {
            server.handler = this
            server.start()
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }

    }

    val redirectUri: String
        get() = "http://localhost:$port/callback"

    @Throws(IOException::class)
    fun waitForCode(): T {
        try {
            return f.get(60, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            throw IOException(e)
        } catch (e: ExecutionException) {
            throw IOException(e)
        } catch (e: TimeoutException) {
            throw IOException(e)
        }

    }

    @Throws(IOException::class, ServletException::class)
    override fun handle(target: String,
                        baseRequest: Request,
                        request: HttpServletRequest,
                        response: HttpServletResponse) {
        response.contentType = "text/html; charset=utf-8"
        response.status = HttpServletResponse.SC_OK

        val out = response.writer

        val error = request.getParameter("error")

        if (error != null) {
            out.println(generateFailBody(request, error))
        } else {
            out.println(generateSuccessBody(request))
        }
        out.flush()
        out.close()

        // return response before continuing

        if (error != null) {
            f.completeExceptionally(IOException(error))
        } else {
            f.complete(codeReader.apply(request))
        }

        baseRequest.isHandled = true

        val t = Thread(Runnable { this.shutdown() }, "SimpleWebServer Stop")
        t.isDaemon = true
        t.start()
    }

    private fun shutdown() {
        try {
            for (c in getServer().connectors) {
                c.shutdown()
            }
            server.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun generateSuccessBody(request: HttpServletRequest): String {

        return "<html>\n"
        +"<body background=\"http://win.blogadda.com/wp-content/uploads/2015/08/inspire-win-15.jpg\">\n"
        +"<h1>Authorization Token Received!</h1>\n"
        +"</body>\n"
        +"</html>"
    }

    private fun generateFailBody(request: HttpServletRequest, error: String): String {
        val params = request.parameterMap
                .entries
                .stream()
                .map { e -> e.key + " = " + Arrays.stream(e.value).collect<String, *>(joining(", ")) }
                .collect<String, *>(joining("<br/>"))

        return "<html>\n"
        +"<body background=\"http://adsoftheworld.com/sites/default/files/fail_moon_aotw.jpg\">\n"
        +"<h1>Authorization Error!</h1>\n"
        +"<p style=\"font-size: 600%; font-family: Comic Sans, Comic Sans MS, cursive;\">"
        +error
        +"</p>"
        +"<p>"
        +params
        +"</p>"
        +"</body>\n"
        +"</html>"
    }

    override fun close() {
        shutdown()
    }

    companion object {

        @Throws(IOException::class)
        fun forCode(): SimpleWebServer<String> {
            return SimpleWebServer({ r -> r.getParameter("code") })
        }

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            SimpleWebServer.forCode().waitForCode()
        }
    }
}
