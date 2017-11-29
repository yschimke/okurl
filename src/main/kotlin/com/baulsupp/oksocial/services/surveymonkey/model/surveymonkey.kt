package com.baulsupp.oksocial.services.surveymonkey.model

data class SurveyInfo(val href: String, val nickname: String, val id: String, val title: String)

data class SurveyList(val page: Int, val total: Int, val data: List<SurveyInfo>)
