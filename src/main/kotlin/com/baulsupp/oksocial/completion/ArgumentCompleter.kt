package com.baulsupp.oksocial.completion

interface ArgumentCompleter {
  suspend fun urlList(prefix: String): UrlList
}
