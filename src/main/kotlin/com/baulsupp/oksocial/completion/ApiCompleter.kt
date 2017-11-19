package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl
import java.io.IOException
import java.util.concurrent.CompletableFuture

interface ApiCompleter {
  @Throws(IOException::class)
  fun prefixUrls(): CompletableFuture<UrlList>

  @Throws(IOException::class)
  fun siteUrls(url: HttpUrl): CompletableFuture<UrlList>
}
