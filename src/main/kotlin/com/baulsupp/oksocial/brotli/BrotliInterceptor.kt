package com.baulsupp.oksocial.brotli

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Okio
import org.brotli.dec.BrotliInputStream

/**
 * Transparent Brotli response support.
 *
 * Adds Accept-Encoding: br to existing encodings and checks (and strips) for Content-Encoding: br in responses
 */
object BrotliInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder().addHeader("Accept-Encoding", "br").build()

    val response = chain.proceed(request)

    if (response.header("Content-Encoding") == "br") {
      val body = response.body()!!
      val decompressedSource = Okio.buffer(Okio.source(BrotliInputStream(body.source().inputStream())))
      return response.newBuilder()
        .removeHeader("Content-Encoding")
        .body(ResponseBody.create(body.contentType(), -1, decompressedSource))
        .build()
    }

    return response
  }
}