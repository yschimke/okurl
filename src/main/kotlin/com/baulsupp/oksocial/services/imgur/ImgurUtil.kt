package com.baulsupp.oksocial.services.imgur

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object ImgurUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.imgur.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.imgur.com" + s).build()
    }
}
