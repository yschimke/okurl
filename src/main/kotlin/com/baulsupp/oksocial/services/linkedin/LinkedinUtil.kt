package com.baulsupp.oksocial.services.linkedin

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object LinkedinUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.linkedin.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.linkedin.com" + s).build()
    }
}
