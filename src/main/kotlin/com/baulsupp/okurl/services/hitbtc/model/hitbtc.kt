package com.baulsupp.okurl.services.hitbtc.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Currency(
  val id: String,
  val fullName: String,
  val crypto: Boolean,
  val payinEnabled: Boolean,
  val payinPaymentId: Boolean,
  val payinConfirmations: Int,
  val payoutEnabled: Boolean,
  val payoutIsPaymentId: Boolean,
  val transferEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class Balance(
  val currency: String,
  val available: String,
  val reserved: String
)

@JsonClass(generateAdapter = true)
data class Ticker(
  val symbol: String,
  val ask: String,
  val bid: String,
  val last: String,
  val low: String,
  val high: String,
  val open: String,
  val volume: String,
  val volumeQuoute: String,
  val timestamp: String
)

@JsonClass(generateAdapter = true)
data class PublicTrade(
  val id: Int,
  val price: String,
  val quantity: String,
  val side: String,
  val timestamp: String
)

@JsonClass(generateAdapter = true)
data class Depth(
  val price: String,
  val size: String
)

@JsonClass(generateAdapter = true)
data class Orderbook(
  val ask: List<Depth>,
  val bid: List<Depth>,
  val timestamp: String
)

@JsonClass(generateAdapter = true)
data class TradingFee(
  val takeLiquidityRate: String,
  val provideLiquidityRate: String
)

@JsonClass(generateAdapter = true)
data class Symbol(
  val id: String,
  val baseCurrency: String,
  val quoteCurrency: String,
  val quantityIncrement: String,
  val tickSize: String,
  val takeLiquidityRate: String,
  val provideLiquidityRate: String,
  val feeCurrency: String
)

@JsonClass(generateAdapter = true)
data class Order(
  val id: Int,
  val clientOrderId: String,
  val symbol: String,
  val side: String,
  val status: String,
  val type: String,
  val timeInForce: String,
  val quantity: String,
  val price: String,
  val cumQuantity: String,
  val createdAt: String,
  val updatedAt: String,
  val stopPrice: String,
  val expireTime: String,
  val tradesReport: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class Trade(
  val id: Int,
  val clientOrderId: String,
  val orderId: Int,
  val symbol: String,
  val side: String,
  val quantity: String,
  val fee: String,
  val price: String,
  val timestamp: String
)

@JsonClass(generateAdapter = true)
data class Transaction(
  val id: String,
  val index: String,
  val currency: String,
  val amount: String,
  val fee: String,
  val networkFee: String,
  val address: String,
  val paymentId: String,
  val hash: String,
  val status: String,
  val type: String,
  val createdAt: String,
  val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class Address(
  val address: String,
  val paymentId: String
)

@JsonClass(generateAdapter = true)
data class WithdrawConfirm(
  val result: Boolean
)

@JsonClass(generateAdapter = true)
data class Candle(
  val timestamp: String,
  val open: String,
  val close: String,
  val min: String,
  val max: String,
  val volume: String,
  val volumeQuote: String
)
