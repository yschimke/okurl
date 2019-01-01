package com.baulsupp.okurl.services.postman.model

data class User(val id: String)

data class UserResult(val user: User)

data class CollectionSummary(val id: String, val name: String, val owner: String, val uid: String)

data class CollectionsResult(val collections: List<CollectionSummary>)

data class CollectionInfo(val name: String, val description: String, val schema: String, val _postman_id: String)

// url
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

data class RequestStruct(val name: String, val _postman_id: String, val request: Request?)

data class CollectionItem(
  val _postman_id: String,
  val description: String?,
  val name: String,
  val item: List<RequestStruct>
)

data class Collection(val info: CollectionInfo, val item: List<CollectionItem>)

data class CollectionResult(val collection: Collection)
