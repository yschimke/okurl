package com.baulsupp.oksocial.services.mapbox

import okhttp3.Request

object MapboxUtil {

  val API_HOSTS = setOf((
      "api.mapbox.com")
  )

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.mapbox.com" + s).build()
  }
}
