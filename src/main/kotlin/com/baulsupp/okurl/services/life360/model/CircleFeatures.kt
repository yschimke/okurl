package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CircleFeatures(
    val locationUpdatesLeft: Int,
    val ownerId: String?,
    val premium: String,
    val priceMonth: String,
    val priceYear: String,
    val skuId: String?,
    val skuTier: String?
)
