package com.baulsupp.oksocial.services.twilio

import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object TwilioUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.twilio.com")
    )

    fun apiRequest(path: String,
                   requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.twilio.com" + path).build()
    }

    fun accountRequest(credentials: BasicCredentials, path: String,
                       requestBuilder: Request.Builder): Request {
        return requestBuilder.url(
                "https://api.twilio.com/2010-04-01/Accounts/" + credentials.user + path).build()
    }
}
