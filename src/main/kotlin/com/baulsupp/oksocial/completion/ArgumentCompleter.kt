package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Token

interface ArgumentCompleter {
  suspend fun urlList(prefix: String, tokenSet: Token): UrlList
}
