package com.baulsupp.oksocial.tracing;

import brave.Span;
import brave.Tracer;
import brave.http.HttpTracing;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Protocol;

public class ZipkinTracingListener extends EventListener {
  private final Call call;
  private final Tracer tracer;
  private final HttpTracing tracing;

  private Span connectSpan;
  private Span dnsSpan;
  private boolean connectionEvent = false;
  private Span callSpan;

  public ZipkinTracingListener(Call call, Tracer tracer, HttpTracing tracing) {
    this.call = call;
    this.tracer = tracer;
    this.tracing = tracing;
  }

  @Override public void fetchStart(Call call) {
    //callSpan = tracer.newTrace().name("http").start();
  }

  @Override public void fetchEnd(Call call, Throwable throwable) {
    //callSpan.finish();
  }

  @Override public void dnsStart(Call call, String domainName) {
    dnsSpan =
        tracer.newChild(tracing.tracing().currentTraceContext().get()).start().name("dns");
  }

  @Override
  public void dnsEnd(Call call, String domainName, @Nullable List<InetAddress> inetAddressList,
      @Nullable Throwable throwable) {
    dnsSpan.finish();
    dnsSpan = null;
  }

  @Override public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    connectSpan =
        tracer.newChild(tracing.tracing().currentTraceContext().get()).start().name("connect");
  }

  @Override public void connectEnd(Call call, InetSocketAddress inetSocketAddress,
      @Nullable Protocol protocol, @Nullable Throwable throwable) {
    connectSpan.finish();
    connectSpan = null;

    connectionEvent = true;
  }

  @Override public void connectionFound(Call call, Connection connection) {
    if (!connectionEvent) {
      tracer.newChild(tracing.tracing().currentTraceContext().get())
          .start()
          .name("connect")
          .finish();
    }
  }


}
