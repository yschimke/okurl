package com.baulsupp.okurl.services.mapbox

import com.baulsupp.okurl.location.Location
import java.net.URLEncoder

class StaticMapBuilder {
  val markers = mutableListOf<String>()

  fun pinLocation(loc: Location?, markerType: String? = "m-marker+CCC") {
    if (loc != null) {
      markers.add("pin-m-marker+CCC(" + loc.longitude + "," + loc.latitude + ")")
    }
  }

  fun pinLocations(locs: Iterable<Location>, markerType: String? = "m-marker+CCC") {
    locs.forEach { pinLocation(it, markerType) }
  }

  fun route(r: String?) {
    if (r != null) {
      markers.add("path-2+f44-0.75+f44-0.0(${URLEncoder.encode(r, "UTF-8")})")
    }
  }
}

fun staticMap(init: StaticMapBuilder.() -> Unit = {}): String {
  val markers = StaticMapBuilder().apply(init).markers
  return "https://api.mapbox.com/v4/mapbox.dark/${markers.joinToString(",")}/auto/400x400.png"
}
