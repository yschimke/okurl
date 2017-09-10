package com.baulsupp.oksocial.tracing

import brave.Span
import brave.Tracer
import brave.http.HttpTracing
import brave.propagation.TraceContext
import okhttp3.*
import zipkin.TraceKeys
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.function.Consumer

class ZipkinTracingListener(private val call: Call, private val tracer: Tracer, private val tracing: HttpTracing,
                            private val opener: Consumer<TraceContext>, private val detailed: Boolean) : EventListener() {

    private var connectSpan: Span? = null
    private var dnsSpan: Span? = null
    private var callSpan: Span? = null
    private var spanInScope: Tracer.SpanInScope? = null
    private var requestSpan: Span? = null
    private var responseSpan: Span? = null
    private var secureConnectSpan: Span? = null
    private var connectionSpan: Span? = null

    override fun callStart(call: Call?) {
        callSpan = tracer.newTrace().name("http").start()

        if (!callSpan!!.isNoop) {
            callSpan!!.tag(TraceKeys.HTTP_PATH, call!!.request().url().encodedPath())
            callSpan!!.tag(TraceKeys.HTTP_METHOD, call.request().method())
            callSpan!!.tag(TraceKeys.HTTP_HOST, call.request().url().host())
            callSpan!!.kind(Span.Kind.CLIENT)
        }

        spanInScope = tracer.withSpanInScope(callSpan)
    }

    override fun callEnd(call: Call?) {
        if (callSpan!!.isNoop) {
            return
        }

        spanInScope!!.close()
        callSpan!!.finish()

        opener.accept(callSpan!!.context())
    }

    override fun callFailed(call: Call?, ioe: IOException?) {
        if (callSpan!!.isNoop) {
            return
        }

        callSpan!!.tag("error", ioe!!.toString())

        callEnd(call)
    }

    override fun dnsStart(call: Call?, domainName: String?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        dnsSpan = tracer.newChild(callSpan!!.context()).start().name("dns")
    }

    override fun dnsEnd(call: Call?, domainName: String?, inetAddressList: List<InetAddress>?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        dnsSpan!!.tag("dns.results",
                inetAddressList!!.map({ it.toString() }).joinToString(", "))

        dnsSpan!!.finish()
    }

    override fun connectStart(call: Call?, inetSocketAddress: InetSocketAddress?, proxy: Proxy?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        connectSpan = tracer.newChild(callSpan!!.context()).start().name("connect")

        connectSpan!!.tag("host", inetSocketAddress!!.toString())
        connectSpan!!.tag("proxy", proxy!!.toString())
    }

    override fun connectEnd(call: Call?, inetSocketAddress: InetSocketAddress?, proxy: Proxy?,
                            protocol: Protocol?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        connectSpan!!.tag("protocol", protocol!!.toString())

        connectSpan!!.finish()
    }

    override fun connectFailed(call: Call?, inetSocketAddress: InetSocketAddress?, proxy: Proxy?,
                               protocol: Protocol?, ioe: IOException?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        if (protocol != null) {
            connectSpan!!.tag("protocol", protocol.toString())
        }
        connectSpan!!.tag("failed", ioe!!.toString())

        connectSpan!!.finish()
    }

    override fun connectionAcquired(call: Call?, connection: Connection?) {
        if (callSpan!!.isNoop) {
            return
        }

        connectionSpan = tracer.newChild(callSpan!!.context()).start().name("connection")
        connectionSpan!!.tag("route", connection!!.route().socketAddress().toString())
    }

    override fun connectionReleased(call: Call?, connection: Connection?) {
        if (callSpan!!.isNoop) {
            return
        }

        if (connection!!.route().proxy().type() != Proxy.Type.DIRECT) {
            connectionSpan!!.tag("proxy", connection.route().proxy().toString())
        }
        if (connection.handshake() != null) {
            connectionSpan!!.tag("cipher", connection.handshake()!!.cipherSuite().toString())
            connectionSpan!!.tag("peer", connection.handshake()!!.peerPrincipal()!!.toString())
            connectionSpan!!.tag("tls", connection.handshake()!!.tlsVersion().toString())
        }
        if (connection.protocol() != null) {
            connectionSpan!!.tag("protocol", connection.protocol().toString())
        }

        connectionSpan!!.finish()
    }

    override fun secureConnectStart(call: Call?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        secureConnectSpan = tracer.newChild(callSpan!!.context()).start().name("tls")
    }

    override fun secureConnectEnd(call: Call?, handshake: Handshake?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        secureConnectSpan!!.finish()
    }

    override fun requestHeadersStart(call: Call?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        requestSpan = tracer.newChild(callSpan!!.context()).start().name("request")
    }

    override fun requestHeadersEnd(call: Call?, request: Request?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        requestSpan!!.tag("requestHeaderLength", "" + request!!.headers().byteCount())
    }

    override fun requestBodyEnd(call: Call?, bytesWritten: Long) {
        if (callSpan!!.isNoop) {
            return
        }

        requestSpan!!.tag("requestBodyBytes", "" + bytesWritten)

        requestSpan = finish(requestSpan)
    }

    private fun finish(span: Span?): Span? {
        span?.finish()
        return null
    }

    override fun responseHeadersStart(call: Call?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        requestSpan = finish(requestSpan)

        responseSpan = tracer.newChild(callSpan!!.context()).start().name("response")
    }

    override fun responseHeadersEnd(call: Call?, response: Response?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        responseSpan!!.tag("responseHeaderLength", "" + response!!.headers().byteCount())
    }

    override fun responseBodyEnd(call: Call?, bytesRead: Long) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }

        responseSpan!!.tag("responseBodyBytes", "" + bytesRead)

        responseSpan = finish(responseSpan)
    }

    override fun responseBodyStart(call: Call?) {
        if (callSpan!!.isNoop || !detailed) {
            return
        }
    }
}
