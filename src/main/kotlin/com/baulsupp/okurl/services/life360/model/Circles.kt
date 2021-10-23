package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Circles(
  val circles: List<Circle>
)
