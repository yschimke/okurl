package com.baulsupp.oksocial.services.giphy

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object GiphyUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.giphy.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.giphy.com" + s).build()
    }
}
