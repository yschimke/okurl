package com.baulsupp.oksocial.services.paypal

import okhttp3.Request

object PaypalUtil {

    val API_HOSTS = setOf(
            "api.paypal.com", "api.sandbox.paypal.com"
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.paypal.com" + s).build()
    }
}
