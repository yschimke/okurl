package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl

interface ApiCompleter {
  suspend fun prefixUrls(): UrlList

  suspend fun siteUrls(url: HttpUrl): UrlList
}
