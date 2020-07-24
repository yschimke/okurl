package com.baulsupp.okurl.services.postman.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(val id: String)

@JsonClass(generateAdapter = true)
data class UserResult(val user: User)

@JsonClass(generateAdapter = true)
data class CollectionSummary(val id: String, val name: String, val owner: String, val uid: String)

@JsonClass(generateAdapter = true)
data class CollectionsResult(val collections: List<CollectionSummary>)

@JsonClass(generateAdapter = true)
data class CollectionInfo(val name: String, val description: String, val schema: String, val _postman_id: String)

@JsonClass(generateAdapter = true)
data class Request(val method: String, val url: Any) {
  fun urlString(): String? {
    return when (url) {
      is String -> url
      is Map<*, *> -> url["raw"] as? String
      else -> null
    }
  }

  fun urlStruct(): Map<*, *>? {
    return url as? Map<*, *>
  }
}

@JsonClass(generateAdapter = true)
data class RequestStruct(val name: String, val _postman_id: String, val request: Request?)

@JsonClass(generateAdapter = true)
data class CollectionItem(
  val _postman_id: String,
  val description: String?,
  val name: String,
  val item: List<RequestStruct>
)

@JsonClass(generateAdapter = true)
data class Collection(val info: CollectionInfo, val item: List<CollectionItem>)

@JsonClass(generateAdapter = true)
data class CollectionResult(val collection: Collection)
