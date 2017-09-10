package com.baulsupp.oksocial.services.imgur

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object ImgurUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.imgur.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.imgur.com" + s).build()
    }
}
