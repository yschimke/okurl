package com.baulsupp.oksocial.services.paypal

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object PaypalUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.paypal.com", "api.sandbox.paypal.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.paypal.com" + s).build()
    }
}
