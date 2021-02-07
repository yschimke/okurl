package com.baulsupp.okurl.test

fun main() {
  com.baulsupp.okurl.main(
    "--debug --urlCompletion https://api.coingecko.com/api/v3/".split(" ").toTypedArray())
}
