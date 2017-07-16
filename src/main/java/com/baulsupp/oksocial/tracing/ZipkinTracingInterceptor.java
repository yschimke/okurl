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

  public ZipkinTracingInterceptor(Tracing tracing) {

    this.tracing = tracing;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    TraceContext traceContext = tracing.currentTraceContext().get();
    if (traceContext != null) {
      Request.Builder newRequest = request.newBuilder();

      TraceContext.Injector<Request.Builder> i =
          B3Propagation.create(Propagation.KeyFactory.STRING).injector(
              (request1, header, value) -> request1.header(header, value));

      i.inject(traceContext, newRequest);

      return chain.proceed(newRequest.build());
    } else {
      return chain.proceed(request);
    }
  }
}
