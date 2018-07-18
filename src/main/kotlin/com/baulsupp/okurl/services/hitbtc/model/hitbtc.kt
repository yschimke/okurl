package com.baulsupp.okurl.services.hitbtc.model

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

data class Balance(
  val currency: String,
  val available: String,
  val reserved: String
)

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

data class PublicTrade(
  val id: Int,
  val price: String,
  val quantity: String,
  val side: String,
  val timestamp: String
)

data class Depth(
  val price: String,
  val size: String
)

data class Orderbook(
  val ask: List<Depth>,
  val bid: List<Depth>,
  val timestamp: String
)

data class TradingFee(
  val takeLiquidityRate: String,
  val provideLiquidityRate: String
)

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

data class Address(
  val address: String,
  val paymentId: String
)

data class WithdrawConfirm(
  val result: Boolean
)

data class Candle(
  val timestamp: String,
  val open: String,
  val close: String,
  val min: String,
  val max: String,
  val volume: String,
  val volumeQuote: String
)
