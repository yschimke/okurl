package com.baulsupp.oksocial.services.microsoft

import okhttp3.Request

object MicrosoftUtil {

    val API_HOSTS = setOf((
            "graph.microsoft.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://graph.microsoft.com" + s).build()
    }
}
