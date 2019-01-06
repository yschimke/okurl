package com.baulsupp.okurl.services.coinbin.model

data class Coin(val name: String, val btc: Double, val usd: Double, val rank: Int, val ticker: String)

data class Coins(val coins: Map<String, Coin>)
