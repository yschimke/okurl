package com.baulsupp.oksocial.services.transferwise

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object TransferwiseUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.transferwise.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.transferwise.com" + s).build()
    }
}
