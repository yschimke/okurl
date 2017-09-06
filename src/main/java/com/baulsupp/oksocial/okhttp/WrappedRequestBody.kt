package com.baulsupp.oksocial.okhttp

import java.io.IOException
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class WrappedRequestBody(private val body: RequestBody, private val contentType: String) : RequestBody() {

    override fun contentType(): MediaType? {
        return MediaType.parse(contentType)
    }

    @Throws(IOException::class)
    override fun writeTo(bufferedSink: BufferedSink) {
        body.writeTo(bufferedSink)
    }
}
