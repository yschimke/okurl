package com.baulsupp.okurl.okhttp

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class WrappedRequestBody(private val body: RequestBody, private val contentType: String) : RequestBody() {

  override fun contentType(): MediaType {
    return MediaType.get(contentType)
  }

  override fun writeTo(sink: BufferedSink) {
    body.writeTo(sink)
  }
}
