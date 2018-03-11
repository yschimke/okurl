package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.credentials.Token

interface ArgumentCompleter {
  suspend fun urlList(prefix: String, tokenSet: Token): UrlList
}
