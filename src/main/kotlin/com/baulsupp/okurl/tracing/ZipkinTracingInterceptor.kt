package com.baulsupp.okurl.tracing

import brave.Tracing
import brave.propagation.TraceContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ZipkinTracingInterceptor(private val tracing: Tracing) : Interceptor {
  private val injector: TraceContext.Injector<Request.Builder>

  init {
    injector = tracing.propagation().injector { request, header, value -> request.header(header, value) }
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val traceContext = tracing.currentTraceContext().get() // expect an existing trace
    ?: return chain.proceed(request)

    val newRequest = request.newBuilder()

    injector.inject(traceContext, newRequest)

    return chain.proceed(newRequest.build())
  }
}
