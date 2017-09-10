package com.baulsupp.oksocial.services.surveymonkey

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object SurveyMonkeyUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.surveymonkey.net")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.surveymonkey.net" + s).build()
    }
}
