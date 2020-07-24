package com.baulsupp.okurl.services.oxforddictionaries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ODToken(val appId: String, val appKey: String)
