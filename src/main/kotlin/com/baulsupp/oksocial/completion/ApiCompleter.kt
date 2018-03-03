package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Token
import okhttp3.HttpUrl

interface ApiCompleter {
  /**
   * Top level, usually host level completion.
   */
  suspend fun prefixUrls(): UrlList

  /** Site specific url completion usually within a single service e.g. https://api.twitter.com/ */
  suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList
}
