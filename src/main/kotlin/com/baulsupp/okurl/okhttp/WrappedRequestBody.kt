package com.baulsupp.okurl.okhttp

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink

class WrappedRequestBody(private val body: RequestBody, private val contentType: String) : RequestBody() {

  override fun contentType(): MediaType {
    return contentType.toMediaType()
  }

  override fun writeTo(sink: BufferedSink) {
    body.writeTo(sink)
  }
}
