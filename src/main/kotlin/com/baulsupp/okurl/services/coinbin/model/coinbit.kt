package com.baulsupp.okurl.services.coinbin.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Coin(val name: String, val btc: Double, val usd: Double, val rank: Int, val ticker: String)

@JsonClass(generateAdapter = true)
data class Coins(val coins: Map<String, Coin>)
