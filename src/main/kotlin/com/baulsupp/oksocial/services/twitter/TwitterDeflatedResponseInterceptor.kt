package com.baulsupp.oksocial.services.twitter

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.InflaterSource
import okio.Okio
import java.io.IOException
import java.util.zip.Inflater

class TwitterDeflatedResponseInterceptor : Interceptor {
  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    if ("deflate" == response.header("content-encoding")) {
      val host = response.request().url().host()

      if (TwitterUtil.TWITTER_HOSTS.contains(host)) {
        return response.newBuilder()
                .body(inflateBody(response.body()))
                .removeHeader("content-encoding")
                .removeHeader("content-length")
                .build()
      }
    }

    return response
  }

  private fun inflateBody(origBody: ResponseBody?): ResponseBody {
    val inflater = Inflater()
    val realSource = origBody!!.source()
    val s = InflaterSource(realSource, inflater)

    return ResponseBody.create(origBody.contentType(), -1, Okio.buffer(s))
  }
}
