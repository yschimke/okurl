package com.baulsupp.oksocial.completion

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import java.util.concurrent.CompletableFuture

abstract class ContinuationApiCompleter : ApiCompleter {
  override fun prefixUrls(): CompletableFuture<UrlList> {
    val future = CompletableFuture<UrlList>()

    launch(CommonPool, true, {
      try {
        future.complete(prefix())
      } catch (e: Exception) {
        future.completeExceptionally(e)
      }
    })

    return future
  }

  override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
    val future = CompletableFuture<UrlList>()

    launch(CommonPool, true, {
      try {
        future.complete(site(url))
      } catch (e: Exception) {
        future.completeExceptionally(e)
      }
    })

    return future
  }

  abstract suspend fun prefix(): UrlList

  abstract suspend fun site(url: HttpUrl): UrlList
}