package com.baulsupp.oksocial.services.squareup.model

data class Location(val id: String, val name: String, val address: Map<String, String>?,
                    val timezone: String?, val capabilities: List<String>?, val status: String?,
                    val country: String?, val phone_number: String?)

data class LocationList(val locations: List<Location>)

data class User(val id: String, val name: String, val email: String)
