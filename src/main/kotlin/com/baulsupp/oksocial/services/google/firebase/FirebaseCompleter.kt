package com.baulsupp.oksocial.services.google.firebase

import com.baulsupp.oksocial.credentials.Token
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
  override suspend fun prefixUrls(): UrlList = UrlList(UrlList.Match.HOSTS, HostUrlCompleter.hostUrls(hosts(), false))

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    val results = siblings(url, tokenSet) + children(url, tokenSet)

    val candidates = results.map { url.newBuilder().encodedPath(it).build().toString() }

    logger.fine("candidates $candidates")

    return UrlList(UrlList.Match.EXACT, dedup(candidates + thisNode(url)))
  }

  private fun dedup(candidates: List<String>) = candidates.toSortedSet().toList()

  suspend fun thisNode(url: HttpUrl): List<String> {
    val path = url.encodedPath()

    return if (path.endsWith("/")) {
      listOf("$url.json")
    } else if (path.endsWith(".json") || url.querySize() > 0) {
      listOf(url.toString())
    } else if (path.contains('.')) {
      listOf(url.toString().replaceAfterLast(".", "json"))
    } else {
      listOf()
    }
  }

  suspend fun siblings(url: HttpUrl, tokenSet: Token): List<String> {
    return if (url.encodedPath() == "/" || url.querySize() > 1 || url.encodedPath().contains(".")) {
      listOf()
    } else {
      val parentPath = url.encodedPath().replaceAfterLast("/", "")

      val encodedPath = url.newBuilder().encodedPath("$parentPath.json")
      val siblings = keyList(encodedPath, tokenSet)

      siblings.toList().flatMap { listOf("$parentPath$it", "$parentPath$it.json") }
    }
  }

  suspend fun keyList(encodedPath: HttpUrl.Builder, tokenSet: Token): List<String> {
    val request = request(tokenSet = tokenSet) { encodedPath.addQueryParameter("shallow", "true").build() }
    return client.queryOptionalMap<Any>(request)?.keys?.toList().orEmpty()
  }

  suspend fun children(url: HttpUrl, tokenSet: Token): List<String> {
    return if (url.querySize() > 1 || url.encodedPath().contains(".")) {
      listOf()
    } else {
      val path = url.encodedPath()

      val encodedPath = url.newBuilder().encodedPath("$path.json")
      val children = keyList(encodedPath, tokenSet)

      val prefixPath = if (path.endsWith("/")) path else "$path/"

      children.toList().flatMap { listOf("$prefixPath$it/", "$prefixPath$it.json") }
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
