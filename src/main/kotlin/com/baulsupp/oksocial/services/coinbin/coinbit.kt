package com.baulsupp.oksocial.services.coinbin

data class Coin(val name: String, val btc: Double, val usd: Double, val rank: Int, val ticker: String)

data class Coins(val coins: Map<String, Coin>)