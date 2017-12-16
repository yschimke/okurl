package com.baulsupp.oksocial.services.google.firebase

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.DirCompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.util.FileUtil
import okhttp3.HttpUrl
import java.io.File

class FirebaseCompleter(private val completionCache: CompletionVariableCache = firebaseCache) : ApiCompleter {
  suspend override fun prefixUrls(): UrlList = UrlList(UrlList.Match.HOSTS, HostUrlCompleter.hostUrls(hosts(), false))

  suspend override fun siteUrls(url: HttpUrl): UrlList {
    val host = url.host()
    val path = url.pathSegments()

    return UrlList(UrlList.Match.SITE, listOf(url.toString() + ".json"))
  }

  fun hosts(): List<String> = completionCache["firebase", "hosts"].orEmpty()

  fun registerKnownHost(host: String) {
    val previous = firebaseCache["firebase", "hosts"]

    if (previous == null || !previous.contains(host)) {
      firebaseCache["firebase", "hosts"] = listOf(host) + (previous ?: listOf())
    }
  }

  companion object {
    val firebaseCache = DirCompletionVariableCache(File(FileUtil.oksocialSettingsDir, "firebasehosts.txt"))
  }
}