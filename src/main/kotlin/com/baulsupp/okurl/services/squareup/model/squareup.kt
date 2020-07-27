package com.baulsupp.okurl.services.squareup.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(
  val id: String,
  val name: String,
  val address: Map<String, String>?,
  val timezone: String?,
  val capabilities: List<String>?,
  val status: String?,
  val country: String?,
  val phone_number: String?
)

@JsonClass(generateAdapter = true)
data class LocationList(val locations: List<Location>)

@JsonClass(generateAdapter = true)
data class User(val id: String, val name: String, val email: String)

@JsonClass(generateAdapter = true)
data class AuthDetails(val client_id: String, val client_secret: String, val code: String, val redirect_uri: String, val grant_type: String)
