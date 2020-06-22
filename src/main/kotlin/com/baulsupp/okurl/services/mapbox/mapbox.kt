package com.baulsupp.okurl.services.mapbox

import com.baulsupp.okurl.location.Location
import java.net.URLEncoder

class StaticMapBuilder {
  var style: String = Dark
  val markers = mutableListOf<String>()

  fun pinLocation(loc: Location?, markerType: String? = "m-marker+CCC") {
    if (loc != null) {
      markers.add("pin-$markerType(" + loc.longitude + "," + loc.latitude + ")")
    }
  }

  fun pinLocations(locs: Iterable<Location>, markerType: String? = "m-marker+CCC") {
    locs.forEach { pinLocation(it, markerType) }
  }

  fun route(r: String?) {
    if (r != null) {
      markers.add("path-6+f44-0.75+f44-0.0(${URLEncoder.encode(r, "UTF-8")})")
    }
  }

  companion object {
    const val Light = "light-v10"
    const val Dark = "dark-v10"
    const val Streets = "streets-v11"
    const val Outdoors = "outdoors-v11"
    const val Satellite = "satellite-v9"
    const val SatelliteStreets = "satellite-streets-v11"
  }
}

fun staticMap(init: StaticMapBuilder.() -> Unit = {}): String {
  val builder = StaticMapBuilder().apply(init)
  val markers = builder.markers
  return "https://api.mapbox.com/styles/v1/mapbox/${builder.style}/static/${markers.joinToString(",")}/auto/1024x1024"
}
