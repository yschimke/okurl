package com.baulsupp.oksocial.okhttp

import com.baulsupp.oksocial.output.ResponseExtractor
import com.baulsupp.oksocial.output.util.JsonUtil
import okhttp3.Response
import okio.BufferedSource

class OkHttpResponseExtractor : ResponseExtractor<Response> {
  override fun mimeType(response: Response): String? {
    val host = response.request().url().host()

    val mediaType = response.body()?.contentType()

    return when {
      host == "graph.facebook.com" && mediaType?.subtype() == "javascript" -> JsonUtil.JSON
      host == "dns.google.com" && mediaType?.subtype() == "x-javascript" -> JsonUtil.JSON
      else -> mediaType?.toString()
    }
  }

  override fun source(response: Response): BufferedSource = response.body()!!.source()

  override fun filename(response: Response): String {
    val segments = response.request().url().pathSegments()

    return segments[segments.size - 1]
  }
}
