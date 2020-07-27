package com.baulsupp.okurl.services.coinbase.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Paging(
  val ending_before: String?,
  val starting_after: String?,
  val limit: Int,
  val order: String,
  val previous_uri: String?,
  val next_uri: String?
)

@JsonClass(generateAdapter = true)
open class PageableResult<out T>(open val data: List<T>, open val pagination: Paging)

@JsonClass(generateAdapter = true)
data class Currency(val code: String, val name: String, val color: String, val exponent: Int, val type: String)

@JsonClass(generateAdapter = true)
data class Balance(val amount: Double, val currency: String)

@JsonClass(generateAdapter = true)
data class Account(
  val id: String,
  val name: String,
  val type: String,
  val currency: Currency,
  val balance: Balance,
  val created_at: String,
  val updated_at: String,
  val resource: String,
  val resource_path: String
)

@JsonClass(generateAdapter = true)
data class AccountList(override val data: List<Account>, override val pagination: Paging) :
  PageableResult<Account>(data, pagination)
