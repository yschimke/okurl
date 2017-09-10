package com.baulsupp.oksocial.services.foursquare

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object FourSquareUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.foursquare.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.foursquare.com" + s).build()
    }
}
