package com.baulsupp.oksocial.services.dropbox

import okhttp3.Request

object DropboxUtil {

  val API_HOSTS = setOf("api.dropboxapi.com", "content.dropboxapi.com")

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.dropboxapi.com" + s).build()
  }
}
