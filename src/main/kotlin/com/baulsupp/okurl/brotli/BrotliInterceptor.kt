package com.baulsupp.okurl.brotli

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream

/**
 * Transparent Brotli response support.
 *
 * Adds Accept-Encoding: br to existing encodings and checks (and strips) for Content-Encoding: br in responses
 */
object BrotliInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().newBuilder().header("Accept-Encoding", "br").build()

    val response = chain.proceed(request)

    if (response.header("Content-Encoding") == "br") {
      val body = response.body!!
      val decompressedSource = BrotliInputStream(body.source().inputStream()).source().buffer()
      return response.newBuilder()
        .removeHeader("Content-Encoding")
        .body(decompressedSource.asResponseBody(body.contentType(), -1))
        .build()
    }

    return response
  }
}
