package com.baulsupp.oksocial.services.fitbit

import okhttp3.Request
import java.util.*

object FitbitUtil {
    val SCOPES: Collection<String> = Arrays.asList("activity", "heartrate", "location", "nutrition", "profile",
            "settings", "sleep", "social", "weight")

    val API_HOSTS = setOf((
            "api.fitbit.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.fitbit.com" + s).build()
    }
}
