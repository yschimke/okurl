package com.baulsupp.okurl.completion

import com.baulsupp.okurl.credentials.Token

interface ArgumentCompleter {
  suspend fun urlList(prefix: String, tokenSet: Token): UrlList
}
