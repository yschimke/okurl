package com.baulsupp.oksocial.services.instagram

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object InstagramUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.instagram.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.instagram.com" + s).build()
    }
}
