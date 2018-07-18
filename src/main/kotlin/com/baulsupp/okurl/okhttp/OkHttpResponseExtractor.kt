package com.baulsupp.okurl.okhttp

import com.baulsupp.okurl.kotlin.JSON
import com.baulsupp.oksocial.output.ResponseExtractor
import okhttp3.Response
import okio.BufferedSource

class OkHttpResponseExtractor : ResponseExtractor<Response> {
  override fun mimeType(response: Response): String? {
    val host = response.request().url().host()

    val mediaType = response.body()?.contentType()

    return when {
      host == "graph.facebook.com" && mediaType?.subtype() == "javascript" -> JSON.toString()
      host == "dns.google.com" && mediaType?.subtype() == "x-javascript" -> JSON.toString()
      else -> mediaType?.toString()
    }
  }

  override fun source(response: Response): BufferedSource = response.body()!!.source()

  override fun filename(response: Response): String {
    val segments = response.request().url().pathSegments()

    return segments[segments.size - 1]
  }
}
