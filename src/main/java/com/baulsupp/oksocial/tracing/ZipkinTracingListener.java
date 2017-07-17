package com.baulsupp.oksocial.tracing;

import brave.Span;
import brave.Tracer;
import brave.http.HttpTracing;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
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

  private Span connectSpan;
  private Span dnsSpan;
  private boolean connectionEvent = false;
  private Span callSpan;
  private Tracer.SpanInScope spanInScope;
  private Span requestSpan;
  private Span responseSpan;
  private Span secureConnectSpan;

  public ZipkinTracingListener(Call call, Tracer tracer, HttpTracing tracing) {
    this.call = call;
    this.tracer = tracer;
    this.tracing = tracing;
  }

  @Override public void fetchStart(Call call) {
    callSpan = tracer.newTrace().name("http").start();

    if (callSpan.isNoop()) {
      return;
    }

    callSpan.tag(TraceKeys.HTTP_PATH, call.request().url().encodedPath());
    callSpan.tag(TraceKeys.HTTP_METHOD, call.request().method());
    callSpan.tag(TraceKeys.HTTP_HOST, call.request().url().host());
    callSpan.kind(Span.Kind.CLIENT);

    spanInScope = tracer.withSpanInScope(callSpan);
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    callSpan.finish();
    spanInScope.close();

    if (throwable != null) {
      callSpan.annotate(throwable.toString());
    }
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
      dnsSpan.tag("dns results",
          inetAddressList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    } else {
      dnsSpan.annotate(throwable.toString());
    }

    dnsSpan.finish();
  }

  @Override public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    if (callSpan.isNoop()) {
      return;
    }

    connectSpan =
        tracer.newChild(callSpan.context()).start().name("connect");
  }

  @Override public void connectEnd(Call call, InetSocketAddress inetSocketAddress,
      @Nullable Protocol protocol, @Nullable Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    if (throwable == null) {
      connectSpan.tag("host", inetSocketAddress.toString());
      connectSpan.tag("protocol", protocol.toString());
    } else {
      connectSpan.annotate(throwable.toString());
    }

    connectSpan.finish();

    connectionEvent = true;
  }

  @Override public void connectionFound(Call call, Connection connection) {
    if (callSpan.isNoop()) {
      return;
    }

    if (!connectionEvent) {
      tracer.newChild(callSpan.context())
          .start()
          .name("connect")
          .finish();
    }
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
      secureConnectSpan.annotate(throwable.toString());
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

  @Override public void requestBodyEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    requestSpan.finish();
  }

  @Override public void responseHeadersStart(Call call) {
    if (callSpan.isNoop()) {
      return;
    }

    responseSpan =
        tracer.newChild(callSpan.context()).start().name("response");
  }

  @Override public void responseBodyEnd(Call call, Throwable throwable) {
    if (callSpan.isNoop()) {
      return;
    }

    responseSpan.finish();
  }
}
