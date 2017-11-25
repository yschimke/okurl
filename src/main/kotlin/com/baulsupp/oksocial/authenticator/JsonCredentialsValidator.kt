package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.output.util.JsonUtil
import okhttp3.OkHttpClient
import okhttp3.Request

class JsonCredentialsValidator(
        private var request: Request,
        private var extractor: (Map<String, Any>) -> String,
        private var appRequest: Request? = null,
        private var appExtractor: ((Map<String, Any>) -> String)? = null) {
  init {
    if (appRequest != null) {
      appExtractor!!
    }
  }

  suspend fun validate(client: OkHttpClient): ValidatedCredentials {
    val name = extractString(client.queryForString(request), extractor)
    val app = appRequest?.let { extractString(client.queryForString(it), appExtractor!!) }

    return ValidatedCredentials(name, app)
  }

  fun extractString(responseString: String, responseExtractor: (Map<String, Any>) -> String): String? {
    val map = if (responseString == "") mapOf() else JsonUtil.map(responseString)

    return responseExtractor(map)
  }

  companion object {
    fun fieldExtractor(name: String) = { map: Map<String, Any> -> map[name] }
  }
}
