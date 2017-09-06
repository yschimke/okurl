package com.baulsupp.oksocial.services.mapbox

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object MapboxUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.mapbox.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.mapbox.com" + s).build()
    }
}
