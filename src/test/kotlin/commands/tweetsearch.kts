#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.arguments
import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.execute
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.kotlin.show
import com.baulsupp.oksocial.kotlin.showOutput
import com.baulsupp.oksocial.services.twitter.model.SearchResults
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.net.URLEncoder

var argumentString = arguments.joinToString("+") { URLEncoder.encode(it, "UTF-8") };

runBlocking {
  val results = client.query<SearchResults>(
    "https://api.twitter.com/1.1/search/tweets.json?tweet_mode=extended&q=$argumentString")

  val images = results.statuses.map {
    it.id_str to it.entities?.media?.map {
      async {
        client.execute(request("${it.media_url_https}:thumb"))
      }
    }
  }.toMap()

  results.statuses.forEach { tweet ->
    println("%-20s: %s".format(tweet.user.screen_name, tweet.full_text))

    images[tweet.id_str]?.forEach {
      showOutput(it.await())
      println()
    }
  }
}
