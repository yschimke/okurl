package com.baulsupp.oksocial.services.microsoft

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object MicrosoftUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "graph.microsoft.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://graph.microsoft.com" + s).build()
    }
}
