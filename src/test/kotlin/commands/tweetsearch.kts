#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.services.twitter.model.*
import java.net.URLEncoder

var argumentString = arguments.joinToString("+") { URLEncoder.encode(it, "UTF-8") };

val a = query<SearchResults>(
  "https://api.twitter.com/1.1/search/tweets.json?tweet_mode=extended&q=$argumentString")

a.statuses.forEach { tweet ->
  println("%-20s: %s".format(tweet.user.screen_name, tweet.full_text))

  tweet.entities?.media?.firstOrNull()?.media_url_https?.let {
    show("$it:thumb")
    println()
  }
}
