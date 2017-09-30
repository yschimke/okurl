package com.baulsupp.oksocial.services.transferwise

import okhttp3.Request

object TransferwiseUtil {

    val API_HOSTS = setOf((
            "api.transferwise.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.transferwise.com" + s).build()
    }
}
