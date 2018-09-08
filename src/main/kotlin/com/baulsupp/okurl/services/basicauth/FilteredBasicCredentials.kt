package com.baulsupp.okurl.services.basicauth

import com.baulsupp.okurl.authenticator.BasicCredentials
import okhttp3.HttpUrl

data class FilteredBasicCredentials(val basicCredentials: BasicCredentials, val hostPattern: String) {
  private val patterns by lazy { hostPattern.split('|') }

  fun matches(url: HttpUrl): Boolean = patterns.any { matches(url, it) }

  companion object {
    fun matches(tokens: Collection<FilteredBasicCredentials>, url: HttpUrl): Boolean {
      return tokens.any { it.matches(url) }
    }

    fun matches(url: HttpUrl, pattern: String): Boolean {
      if (pattern == "*")
        return true

      if (pattern == url.host())
        return true

      if (pattern.startsWith("*.") && url.host().endsWith(pattern.substring(1)))
        return true

      return false
    }
  }
}
