package com.baulsupp.oksocial.services.instagram

import okhttp3.Request

object InstagramUtil {

    val API_HOSTS = setOf((
            "api.instagram.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.instagram.com" + s).build()
    }
}
