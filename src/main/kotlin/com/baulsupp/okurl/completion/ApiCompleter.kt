package com.baulsupp.okurl.completion

import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.FileUtil
import okhttp3.Cache
import okhttp3.HttpUrl
import java.io.File

interface ApiCompleter {
  /**
   * Top level, usually host level completion.
   */
  suspend fun prefixUrls(): UrlList

  /** Site specific url completion usually within a single service e.g. https://api.twitter.com/ */
  suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList

  companion object {
    val cache = Cache(File(FileUtil.okurlSettingsDir, "completion-cache"), 256L * 1024 * 1024)
  }
}
