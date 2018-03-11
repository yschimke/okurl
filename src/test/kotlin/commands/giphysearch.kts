#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.*
import com.baulsupp.oksocial.services.giphy.model.*

var results = query<SearchResults>("https://api.giphy.com/v1/gifs/search?q=" + arguments.joinToString("+"))

results.data.map { image ->
  println(image.url)

  image.images["fixed_height_small"]?.url?.let { show(it) }
}
