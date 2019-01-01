package com.baulsupp.okurl.services.gdax.model

data class Account(
  val id: String,
  val currency: String,
  val balance: String,
  val available: String,
  val hold: String,
  val profile_id: String
)

data class Product(
  val id: String,
  val base_currency: String,
  val quote_currency: String,
  val base_min_size: String,
  val base_max_size: String,
  val quote_increment: String,
  val status: String,
  val margin_enabled: Boolean,
  val status_message: String?
)
