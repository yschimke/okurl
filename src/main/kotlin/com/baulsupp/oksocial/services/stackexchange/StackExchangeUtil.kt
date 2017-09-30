package com.baulsupp.oksocial.services.stackexchange

import okhttp3.Request
import java.util.*

object StackExchangeUtil {
    val SCOPES: Collection<String> = Arrays.asList("read_inbox",
            "no_expiry",
            "write_access",
            "private_info")

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.stackexchange.com" + s).build()
    }

    val API_HOSTS = setOf((
            "api.stackexchange.com")
    )
}
