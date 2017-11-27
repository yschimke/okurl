package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.kotlin.queryMap
import okhttp3.OkHttpClient

object CompletionQuery {
  suspend fun getIds(client: OkHttpClient, urlString: String,
          path: String, key: String): List<String> {
    val result = client.queryMap<String, Any>(urlString)
    val surveys = result[path] as List<Map<String, Any>>
    return surveys.map { m -> m[key] as String }
  }
}
