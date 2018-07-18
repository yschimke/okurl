package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.kotlin.requestBuilder
import okhttp3.Request

object TwitterUtil {

  val TWITTER_API_HOSTS = setOf(
    "api.twitter.com", "upload.twitter.com", "stream.twitter.com", "mobile.twitter.com",
    "syndication.twitter.com", "pbs.twimg.com", "t.co", "userstream.twitter.com",
    "sitestream.twitter.com", "search.twitter.com")

  val TWITTER_WEB_HOSTS = setOf(
    "www.twitter.com", "twitter.com")

  val TWITTER_HOSTS = TWITTER_API_HOSTS + TWITTER_WEB_HOSTS

  fun apiRequest(s: String): Request {
    return requestBuilder("https://api.twitter.com$s").build()
  }
}
