package com.baulsupp.oksocial.tracing;

import brave.Span;
import brave.Tracer;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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

  private Span connectSpan;
  private Span dnsSpan;
  private Span callSpan;
  private Tracer.SpanInScope spanInScope;
  private Span requestSpan;
  private Span responseSpan;
  private Span secureConnectSpan;
  private Span connectionSpan;

  public ZipkinTracingListener(Call call, Tracer tracer, HttpTracing tracing,
      Consumer<TraceContext> opener) {
    this.call = call;
    this.tracer = tracer;
    this.tracing = tracing;
    this.opener = opener;
  }

  @Override public void fetchStart(Call call) {
    callSpan = tracer.newTrace().name("http").start();

    if (!callSpan.isNoop()) {
      callSpan.tag(TraceKeys.HTTP_PATH, call.request().url().encodedPath());
      callSpan.tag(TraceKeys.HTTP_METHOD, call.request().method());
      callSpan.tag(TraceKeys.HTTP_HOST, call.request().url().host());
      callSpan.kind(Span.Kind.CLIENT);
    }

    spanInScope = tracer.withSpanInScope(callSpan);
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    callSpan.finish();
    spanInScope.close();

    if (throwable != null) {
      callSpan.tag("error", throwable.toString());
    }

    opener.accept(callSpan.context());
  }

  @Override public void dnsStart(Call call, String domainName) {
    if (callSpan.isNoop()) {
      return;
    }

    dnsSpan =
        tracer.newChild(callSpan.context()).start().name("dns");
  }

  @Override
  public void dnsEnd(Call call, String domainName, @Nullable List<InetAddress> inetAddressList,
      @Nullable Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable == null) {
      dnsSpan.tag("dns.results",
          inetAddressList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    } else {
      dnsSpan.tag("error", throwable.toString());
    }

    dnsSpan.finish();
  }

  @Override public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    if (callSpan.isNoop()) {
      return;
    }

    connectSpan =
        tracer.newChild(callSpan.context()).start().name("connect");

    connectSpan.tag("host", inetSocketAddress.toString());
    connectSpan.tag("proxy", proxy.toString());
  }

  @Override
  public void connectEnd(Call call, InetSocketAddress inetSocketAddress, @Nullable Proxy proxy,
      @Nullable Protocol protocol, @Nullable Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable == null) {
      connectSpan.tag("protocol", protocol.toString());
    } else {
      connectSpan.tag("error", throwable.toString());
    }

    connectSpan.finish();
  }

  @Override public void connectionAcquired(Call call, Connection connection) {
    if (callSpan.isNoop()) {
      return;
    }

    connectionSpan =
        tracer.newChild(callSpan.context()).start().name("connection");
    connectionSpan.annotate(connection.toString());
  }

  @Override public void connectionReleased(Call call, Connection connection) {
    if (callSpan.isNoop()) {
      return;
    }

    connectionSpan.finish();
  }

  @Override public void secureConnectStart(Call call) {
    if (callSpan.isNoop()) {
      return;
    }

    secureConnectSpan =
        tracer.newChild(callSpan.context()).start().name("tls");
  }

  @Override public void secureConnectEnd(Call call, @Nullable Handshake handshake,
      @Nullable Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable == null) {
      secureConnectSpan.tag("handshake", handshake.toString());
    } else {
      secureConnectSpan.tag("error", throwable.toString());
    }

    secureConnectSpan.finish();
  }

  @Override public void requestHeadersStart(Call call) {
    if (callSpan.isNoop()) {
      return;
    }

    requestSpan =
        tracer.newChild(callSpan.context()).start().name("request");
  }

  @Override public void requestHeadersEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable != null) {
      requestSpan.tag("error", throwable.toString());
      requestSpan = finish(requestSpan);
    }
  }

  private Span finish(Span span) {
    if (span != null) {
      span.finish();
    }
    return null;
  }

  @Override public void responseHeadersStart(Call call) {
    if (callSpan.isNoop()) {
      return;
    }

    requestSpan = finish(requestSpan);

    responseSpan =
        tracer.newChild(callSpan.context()).start().name("response");
  }

  @Override public void responseHeadersEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable != null) {
      responseSpan.tag("error", throwable.toString());
      responseSpan = finish(responseSpan);
    }
  }

  @Override public void responseBodyStart(Call call) {
    super.responseBodyStart(call);
  }

  @Override public void responseBodyEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    responseSpan = finish(responseSpan);
  }
}
