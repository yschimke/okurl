package com.baulsupp.okurl.services.coinbase.model

data class Paging(val ending_before: String?, val starting_after: String?, val limit: Int, val order: String, val previous_uri: String?, val next_uri: String?)

open class PageableResult<out T>(open val data: List<T>, open val pagination: Paging)

data class Currency(val code: String, val name: String, val color: String, val exponent: Int, val type: String)

data class Balance(val amount: Double, val currency: String)

data class Account(val id: String, val name: String, val type: String, val currency: Currency, val balance: Balance, val created_at: String, val updated_at: String, val resource: String, val resource_path: String)

data class AccountList(override val data: List<Account>, override val pagination: Paging) :
  PageableResult<Account>(data, pagination)
