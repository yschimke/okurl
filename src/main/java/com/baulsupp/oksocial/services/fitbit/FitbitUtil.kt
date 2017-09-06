package com.baulsupp.oksocial.services.fitbit

import com.google.common.collect.Sets
import java.util.Arrays
import java.util.Collections
import okhttp3.Request

object FitbitUtil {
    val SCOPES: Collection<String> = Arrays.asList("activity", "heartrate", "location", "nutrition", "profile",
            "settings", "sleep", "social", "weight")

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.fitbit.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.fitbit.com" + s).build()
    }
}
