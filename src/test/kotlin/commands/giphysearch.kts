#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.*
import com.baulsupp.okurl.services.giphy.model.SearchResults
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

val size = "preview_gif"

runBlocking {
  val urls = client.query<SearchResults>(
    "https://api.giphy.com/v1/gifs/search?q=" + args.joinToString(
      "+")).data.mapNotNull { it.images[size]?.url }

  val fetches = urls.map {
    async {
      client.execute(request(it))
    }
  }

  fetches.forEach {
    showOutput(it.await())
  }
}
