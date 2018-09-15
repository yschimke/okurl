package com.baulsupp.okurl.services.postman

import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.services.postman.model.CollectionResult
import com.baulsupp.okurl.services.postman.model.CollectionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

suspend fun postmanCollectionUrls(tokenSet: Token): List<String> {
  val collections = client.query<CollectionsResult>(
    "https://api.getpostman.com/collections",
    tokenSet).collections.map { it.id }

  val jobs = collections.map {
    GlobalScope.async(Dispatchers.Default) {
      client.query<CollectionResult>("https://api.getpostman.com/collections/$it", tokenSet)
    }
  }

  return jobs.flatMap {
    it.await().collection.item.flatMap { it.item.map { it.request?.urlString() } }.filterNotNull()
  }
}
