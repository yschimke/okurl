package com.baulsupp.oksocial.completion

import java.io.IOException

interface ArgumentCompleter {
  @Throws(IOException::class)
  fun urlList(prefix: String): UrlList
}
