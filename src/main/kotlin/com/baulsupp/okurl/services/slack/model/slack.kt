package com.baulsupp.okurl.services.slack.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RtmConnect(val ok: Boolean, val url: String)
