package com.baulsupp.oksocial.services.foursquare

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object FourSquareUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.foursquare.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.foursquare.com" + s).build()
    }
}
