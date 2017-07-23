package com.baulsupp.oksocial.tracing;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ZipkinTracingInterceptor implements Interceptor {
  private Tracing tracing;
  private final TraceContext.Injector<Request.Builder> injector;

  public ZipkinTracingInterceptor(Tracing tracing) {
    this.tracing = tracing;
    injector = tracing.propagation().injector(
            (request, header, value) -> request.header(header, value));
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    TraceContext traceContext = tracing.currentTraceContext().get();
    if (traceContext == null) {
      // expect an existing trace
      return chain.proceed(request);
    }

    Request.Builder newRequest = request.newBuilder();

    injector.inject(traceContext, newRequest);

    return chain.proceed(newRequest.build());
  }
}
