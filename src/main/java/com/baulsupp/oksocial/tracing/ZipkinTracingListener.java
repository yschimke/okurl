package com.baulsupp.oksocial.tracing;

import brave.Span;
import brave.Tracer;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import zipkin.TraceKeys;

public class ZipkinTracingListener extends EventListener {
  private final Call call;
  private final Tracer tracer;
  private final HttpTracing tracing;
  private Consumer<TraceContext> opener;
  private boolean detailed;

  private Span connectSpan;
  private Span dnsSpan;
  private Span callSpan;
  private Tracer.SpanInScope spanInScope;
  private Span requestSpan;
  private Span responseSpan;
  private Span secureConnectSpan;
  private Span connectionSpan;

  public ZipkinTracingListener(Call call, Tracer tracer, HttpTracing tracing,
      Consumer<TraceContext> opener, boolean detailed) {
    this.call = call;
    this.tracer = tracer;
    this.tracing = tracing;
    this.opener = opener;
    this.detailed = detailed;
  }

  @Override public void callStart(Call call) {
    callSpan = tracer.newTrace().name("http").start();

    if (!callSpan.isNoop()) {
      callSpan.tag(TraceKeys.HTTP_PATH, call.request().url().encodedPath());
      callSpan.tag(TraceKeys.HTTP_METHOD, call.request().method());
      callSpan.tag(TraceKeys.HTTP_HOST, call.request().url().host());
      callSpan.kind(Span.Kind.CLIENT);
    }

    spanInScope = tracer.withSpanInScope(callSpan);
  }

  @Override public void callEnd(Call call) {
    if (callSpan.isNoop()) {
      return;
    }

    spanInScope.close();
    callSpan.finish();

    opener.accept(callSpan.context());
  }

  @Override public void callFailed(Call call, IOException ioe) {
    if (callSpan.isNoop()) {
      return;
    }

    callSpan.tag("error", ioe.toString());

    callEnd(call);
  }

  @Override public void dnsStart(Call call, String domainName) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    dnsSpan =
        tracer.newChild(callSpan.context()).start().name("dns");
  }

  @Override
  public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    dnsSpan.tag("dns.results",
        inetAddressList.stream().map(Object::toString).collect(Collectors.joining(", ")));

    dnsSpan.finish();
  }

  @Override public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    connectSpan =
        tracer.newChild(callSpan.context()).start().name("connect");

    connectSpan.tag("host", inetSocketAddress.toString());
    connectSpan.tag("proxy", proxy.toString());
  }

  @Override
  public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
      Protocol protocol) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    connectSpan.tag("protocol", protocol.toString());

    connectSpan.finish();
  }

  @Override public void connectionAcquired(Call call, Connection connection) {
    if (callSpan.isNoop()) {
      return;
    }

    connectionSpan =
        tracer.newChild(callSpan.context()).start().name("connection");
    connectionSpan.tag("route", connection.route().socketAddress().toString());
    if (connection.route().proxy().type() != Proxy.Type.DIRECT) {
      connectionSpan.tag("proxy", connection.route().proxy().toString());
    }
    if (connection.handshake() != null) {
      connectionSpan.tag("cipher", connection.handshake().cipherSuite().toString());
      connectionSpan.tag("peer", connection.handshake().peerPrincipal().toString());
      connectionSpan.tag("tls", connection.handshake().tlsVersion().toString());
    }
    if (connection.protocol() != null) {
      connectionSpan.tag("protocol", connection.protocol().toString());
    }
  }

  @Override public void connectionReleased(Call call, Connection connection) {
    if (callSpan.isNoop()) {
      return;
    }

    connectionSpan.finish();
  }

  @Override public void secureConnectStart(Call call) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    secureConnectSpan =
        tracer.newChild(callSpan.context()).start().name("tls");
  }

  @Override public void secureConnectEnd(Call call, Handshake handshake) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    secureConnectSpan.finish();
  }

  @Override public void requestHeadersStart(Call call) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    requestSpan =
        tracer.newChild(callSpan.context()).start().name("request");
  }

  @Override
  public void requestHeadersEnd(Call call, long headerLength) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    requestSpan.tag("requestHeaderLength", "" + headerLength);
  }

  @Override
  public void requestBodyEnd(Call call, long bytesWritten) {
    if (callSpan.isNoop()) {
      return;
    }

    requestSpan.tag("requestBodyBytes", "" + bytesWritten);

    requestSpan = finish(requestSpan);
  }

  private Span finish(Span span) {
    if (span != null) {
      span.finish();
    }
    return null;
  }

  @Override public void responseHeadersStart(Call call) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    requestSpan = finish(requestSpan);

    responseSpan =
        tracer.newChild(callSpan.context()).start().name("response");
  }

  @Override
  public void responseHeadersEnd(Call call, long headerLength) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    responseSpan.tag("responseHeaderLength", "" + headerLength);
  }

  @Override public void responseBodyEnd(Call call, long bytesRead) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }

    responseSpan.tag("responseBodyBytes", "" + bytesRead);

    responseSpan = finish(responseSpan);
  }

  @Override public void responseBodyStart(Call call) {
    if (callSpan.isNoop() || !detailed) {
      return;
    }
  }
}
