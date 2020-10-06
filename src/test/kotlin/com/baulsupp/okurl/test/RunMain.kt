package com.baulsupp.okurl.test

fun main() {
  com.baulsupp.okurl.main(
    "https://www.strava.com/api/v3/athlete/activities?page=1&per_page=1".split(" ").toTypedArray())
}
