package com.baulsupp.oksocial.services.lyft

import com.google.common.collect.Sets
import java.util.Arrays
import java.util.Collections
import okhttp3.Request

object LyftUtil {
    val SCOPES: Collection<String> = Arrays.asList("public",
            "rides.read",
            "offline",
            "rides.request",
            "profile")

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.lyft.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.lyft.com" + s).build()
    }
}
