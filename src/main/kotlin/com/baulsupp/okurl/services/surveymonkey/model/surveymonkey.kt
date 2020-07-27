package com.baulsupp.okurl.services.surveymonkey.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SurveyInfo(val href: String, val nickname: String, val id: String, val title: String)

@JsonClass(generateAdapter = true)
data class SurveyList(val page: Int, val total: Int, val data: List<SurveyInfo>)

@JsonClass(generateAdapter = true)
data class TokenResponse(val access_token: String)
