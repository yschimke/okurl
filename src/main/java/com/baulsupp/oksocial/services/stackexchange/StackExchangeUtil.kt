package com.baulsupp.oksocial.services.stackexchange

import com.google.common.collect.Sets
import java.util.Arrays
import java.util.Collections
import okhttp3.Request

object StackExchangeUtil {
    val SCOPES: Collection<String> = Arrays.asList("read_inbox",
            "no_expiry",
            "write_access",
            "private_info")

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.stackexchange.com" + s).build()
    }

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.stackexchange.com")
    )
}
