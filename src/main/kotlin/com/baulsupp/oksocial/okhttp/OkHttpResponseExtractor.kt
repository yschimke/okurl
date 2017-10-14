package com.baulsupp.oksocial.okhttp

import com.baulsupp.oksocial.output.ResponseExtractor
import com.baulsupp.oksocial.output.util.JsonUtil
import okhttp3.Response
import okio.BufferedSource
import java.util.Optional
import java.util.Optional.of

class OkHttpResponseExtractor : ResponseExtractor<Response> {
    override fun mimeType(response: Response): Optional<String> {
        if (response.body() == null) {
            return Optional.empty()
        }

        val host = response.request().url().host()
        val mediaType = response.body()!!.contentType() ?: return Optional.empty()

        if (host == "graph.facebook.com" && mediaType.subtype() == "javascript") {
            return of(JsonUtil.JSON)
        }

        return if (host == "dns.google.com" && mediaType.subtype() == "x-javascript") {
            of(JsonUtil.JSON)
        } else of(mediaType.toString())

    }

    override fun source(response: Response): BufferedSource {
        return response.body()!!.source()
    }

    override fun filename(response: Response): String {
        val segments = response.request().url().pathSegments()

        return segments[segments.size - 1]
    }
}
