package com.baulsupp.oksocial.services.stackexchange.model

data class Question(val tags: List<String>, val title: String, val answer_count: Int, val link: String, val creation_date: Long)

data class Questions(val items: List<Question>, val has_more: Boolean, val quota_max: Int, val quota_remaining: Int)
