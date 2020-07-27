package com.baulsupp.okurl.services.uber.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UberPriceEstimate(
  @Json(name = "localized_display_name") val localizedDisplayName: String,
  val distance: Double,
  val displayName: String?,
  @Json(name = "product_id") val productId: String,
  @Json(name = "high_estimate") val highEstimate: Double?,
  @Json(name = "low_estimate") val lowEstimate: Double?,
  val duration: Int,
  val estimate: String,
  @Json(name = "currency_code") val currencyCode: String?
)

@JsonClass(generateAdapter = true)
data class UberTimeEstimate(
  @Json(name = "localized_display_name") val localizedDisplayName: String,
  val estimate: Int,
  val displayName: String?,
  @Json(name = "product_id") val productId: String
)

@JsonClass(generateAdapter = true)
data class UberPriceEstimates(val prices: List<UberPriceEstimate>)

@JsonClass(generateAdapter = true)
data class UberTimeEstimates(val times: List<UberTimeEstimate>)
