package com.baulsupp.oksocial.services.squareup

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object SquareUpUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "connect.squareup.com")
    )

    var ALL_PERMISSIONS: Collection<String> = Arrays.asList(
            "MERCHANT_PROFILE_READ",
            "PAYMENTS_READ",
            "SETTLEMENTS_READ",
            "BANK_ACCOUNTS_READ"
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://connect.squareup.com" + s).build()
    }
}
