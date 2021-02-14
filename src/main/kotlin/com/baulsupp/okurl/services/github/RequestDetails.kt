package com.baulsupp.okurl.services.github

data class RequestDetails(val owner: String? = null, val repo: String? = null) {
  companion object {
    val reposRegex = "https://api.github.com/repos/([^/?]+)/(?:([^/?]+)/)?(.*)".toRegex()

    fun fromUrl(url: String): RequestDetails {
      val repoMatch = reposRegex.matchEntire(url)

      return if (repoMatch != null) {
        RequestDetails(repoMatch.groupValues[1], repoMatch.groupValues[2].ifBlank { null })
      } else {
        RequestDetails()
      }
    }
  }
}
