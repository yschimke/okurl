package com.baulsupp.oksocial.brotli

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Okio
import org.brotli.dec.BrotliInputStream

object BrotliInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder().addHeader("Accept-Encoding", "br").build()

    val response = chain.proceed(request)

    if (response.header("Content-Encoding") == "br") {
      val body = response.body()!!
      val decompressedSource = Okio.buffer(Okio.source(BrotliInputStream(body.source().inputStream())))
      val responseBuilder = response.newBuilder()
      responseBuilder.removeHeader("Content-Encoding")
      return responseBuilder.body(ResponseBody.create(body.contentType(), body.contentLength(), decompressedSource)).build()
    }

    return response
  }
}