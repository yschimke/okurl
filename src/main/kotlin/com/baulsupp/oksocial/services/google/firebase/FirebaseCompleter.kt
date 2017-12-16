package com.baulsupp.oksocial.services.google.firebase

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.UrlList
import okhttp3.HttpUrl

class FirebaseCompleter: ApiCompleter {
  suspend override fun prefixUrls(): UrlList {
    return UrlList(UrlList.Match.HOSTS, listOf("https://linen-centaur-133323.firebaseio.com/"))
  }

  suspend override fun siteUrls(url: HttpUrl): UrlList {
    val host = url.host()
    val path = url.pathSegments()

    return UrlList(UrlList.Match.SITE, listOf(url.toString() + ".json"))
  }

  fun hosts(): List<String> {
    return listOf("linen-centaur-133323.firebaseio.com")
  }
}