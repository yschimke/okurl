package com.baulsupp.oksocial.services.surveymonkey

import okhttp3.Request

object SurveyMonkeyUtil {

    val API_HOSTS = setOf((
            "api.surveymonkey.net")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.surveymonkey.net" + s).build()
    }
}
