package com.baulsupp.oksocial.services.google.firebase

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.DirCompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.kotlin.queryOptionalMap
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.util.FileUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.logging.Logger

class FirebaseCompleter(private val client: OkHttpClient) : ApiCompleter {
  suspend override fun prefixUrls(): UrlList = UrlList(UrlList.Match.HOSTS, HostUrlCompleter.hostUrls(hosts(), false))

  suspend override fun siteUrls(url: HttpUrl): UrlList {
    val results = siblings(url) + children(url)

    val candidates = results.map { url.newBuilder().encodedPath(it).build().toString() }

    logger.fine("candidates $candidates")

    return UrlList(UrlList.Match.EXACT, dedup(candidates + thisNode(url)))
  }

  private fun dedup(candidates: List<String>) = candidates.toSortedSet().toList()

  suspend fun thisNode(url: HttpUrl): List<String> {
    val path = url.encodedPath()

    if (path.endsWith("/")) {
      return listOf("$url.json")
    } else if (path.endsWith(".json") || url.querySize() > 0) {
      return listOf(url.toString())
    } else if (path.contains('.')) {
      return listOf(url.toString().replaceAfterLast(".", "json"))
    } else {
      return listOf()
    }
  }

  suspend fun siblings(url: HttpUrl): List<String> {
    if (url.encodedPath() == "/" || url.querySize() > 1 || url.encodedPath().contains(".")) {
      return listOf()
    } else {
      val parentPath = url.encodedPath().replaceAfterLast("/", "")

      val encodedPath = url.newBuilder().encodedPath("$parentPath.json")
      var siblings = keyList(encodedPath)

      return siblings.toList().flatMap { listOf("$parentPath$it", "$parentPath$it.json") }
    }
  }

  suspend fun keyList(encodedPath: HttpUrl.Builder): List<String> {
    val request = encodedPath.addQueryParameter("shallow", "true").build().request()
    return client.queryOptionalMap<Any>(request)?.keys?.toList().orEmpty()
  }

  suspend fun children(url: HttpUrl): List<String> {
    if (url.querySize() > 1 || url.encodedPath().contains(".")) {
      return listOf()
    } else {
      val path = url.encodedPath()

      val encodedPath = url.newBuilder().encodedPath("$path.json")
      var children = keyList(encodedPath)

      val prefixPath = if (path.endsWith("/")) path else path + "/"

      return children.toList().flatMap { listOf(prefixPath + it + "/", prefixPath + it + ".json") }
    }
  }

  fun hosts(): List<String> = knownHosts()

  companion object {
    private val logger = Logger.getLogger(FirebaseCompleter::class.java.name)

    val firebaseCache = DirCompletionVariableCache(FileUtil.oksocialSettingsDir)

    fun knownHosts(): List<String> = firebaseCache["firebase", "hosts"].orEmpty()
    fun registerKnownHost(host: String) {
      val previous = firebaseCache["firebase", "hosts"]

      if (previous == null || !previous.contains(host)) {
        firebaseCache["firebase", "hosts"] = listOf(host) + (previous ?: listOf())
      }
    }
  }
}
