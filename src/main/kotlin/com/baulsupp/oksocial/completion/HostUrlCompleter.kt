package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

open class HostUrlCompleter(private val hosts: Iterable<String>) : ApiCompleter {

  @Throws(IOException::class)
  override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
    return completedFuture(
        UrlList(UrlList.Match.SITE, urls(true)))
  }

  private fun urls(siteOnly: Boolean) = hosts.flatMap {
    if (siteOnly) {
      listOf("https://$it/")
    } else {
      listOf("https://" + it, "https://$it/")
    }
  }

  @Throws(IOException::class)
  override fun prefixUrls(): CompletableFuture<UrlList> {
    return completedFuture(
        UrlList(UrlList.Match.HOSTS, urls(false)))
  }
}
