package com.baulsupp.oksocial.services.google.firebase

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.DirCompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.kotlin.mapAdapter
import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.util.FileUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class FirebaseCompleter(private val client: OkHttpClient) : ApiCompleter {
  suspend override fun prefixUrls(): UrlList = UrlList(UrlList.Match.HOSTS, HostUrlCompleter.hostUrls(hosts(), false))

  suspend override fun siteUrls(url: HttpUrl): UrlList {
    val results = thisNode(url) + siblings(url) + children(url);

    return UrlList(UrlList.Match.EXACT, results.toSortedSet().toList().map { url.newBuilder().encodedPath(it).build().toString() })
  }

  suspend fun thisNode(url: HttpUrl): List<String> {
    val path = url.encodedPath()

    if (path.endsWith(".json")) {
      return listOf(path)
    } else if (path.contains('.')) {
      return listOf(path.replaceAfterLast(".", ".json"))
    } else {
      return listOf(path, path + ".json")
    }
  }

  suspend fun siblings(url: HttpUrl): List<String> {
    val segments = url.pathSegments()

    if (segments.size < 1) {
      return listOf()
    } else {
      val parentPath = segments.dropLast(1).joinToString(prefix = "/", separator = "/")

      val encodedPath = url.newBuilder().encodedPath("$parentPath.json")
      var siblings = keyList(encodedPath)

      return siblings.toList().flatMap { listOf(parentPath + it, parentPath + it + ".json") }
    }
  }

  suspend fun keyList(encodedPath: HttpUrl.Builder): List<String> {
    val queryJson = client.queryForString(encodedPath.addQueryParameter("shallow", "true").build().request())

    if (queryJson == "null") {
      return listOf()
    } else if (queryJson.startsWith("{")) {
      return moshi.mapAdapter<Any>().fromJson(queryJson)!!.keys.toList()
    }

    // TODO warn
    return listOf()
  }

  suspend fun children(url: HttpUrl): List<String> {
    if (url.encodedPath().contains('.')) {
      return listOf()
    }

    val path = url.encodedPath()

    val encodedPath = url.newBuilder().encodedPath("$path.json")
    var children = keyList(encodedPath)

    return children.toList().flatMap { listOf(path + "/" + it + "/", path + "/" + it + ".json") }
  }

  fun hosts(): List<String> = knownHosts()

  companion object {
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