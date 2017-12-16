package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl

open class HostUrlCompleter(private val hosts: Iterable<String>) : ApiCompleter {

  override suspend fun siteUrls(url: HttpUrl): UrlList = UrlList(UrlList.Match.SITE, urls(true))

  private fun urls(siteOnly: Boolean): List<String> = hostUrls(hosts, siteOnly)

  override suspend fun prefixUrls(): UrlList = UrlList(UrlList.Match.HOSTS, urls(false))

  companion object {
    fun hostUrls(h: Iterable<String>, siteOnly: Boolean): List<String> {
      return h.flatMap {
        if (siteOnly) {
          listOf("https://$it/")
        } else {
          listOf("https://" + it, "https://$it/")
        }
      }
    }
  }
}
