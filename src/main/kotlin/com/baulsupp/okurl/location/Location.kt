package com.baulsupp.okurl.location

data class Location(
  val longitude: Double,
  val latitude: Double,
  val course: Int? = null,
  val v_accuracy: Int? = null,
  val speed: Int? = null,
  val h_accuracy: Int? = null,
  val timestamp: Double? = null,
  val altitude: Double? = null
)
