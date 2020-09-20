package com.baulsupp.okurl.test

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("https://www.strava.com/api/v3/athlete/activities?page=1&per_page=1")
}
