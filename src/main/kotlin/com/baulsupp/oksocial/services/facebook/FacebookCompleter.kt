package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.services.facebook.FacebookUtil.VERSION
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.logging.Level
import java.util.logging.Logger

class FacebookCompleter(private val client: OkHttpClient, hosts: Collection<String>) :
        HostUrlCompleter(hosts) {

  override suspend fun siteUrls(url: HttpUrl): UrlList {
    var result = completePath(url.encodedPath())

    if (!url.encodedPath().endsWith("/")) {
      val parentPaths = url.encodedPathSegments()
      parentPaths.removeAt(parentPaths.size - 1)

      val parentPath = "/" + parentPaths.joinToString("/")

      result = result.combine(completePath(parentPath))
    }

    return result
  }

  private fun addPath(prefix: String): (String) -> String {
    return { c: String -> prefix + (if (prefix.endsWith("/")) "" else "/") + c }
  }

  suspend fun topLevel(): List<String> {
    val url = HttpUrl.parse(
            "https://graph.facebook.com/$VERSION/me/accounts?fields=username")
    val request = Request.Builder().url(url!!).build()

    val map = client.queryMap<String, Any>(request)

    // TODO exception handling
    return (map["data"] as List<Map<String, String>>).map { it["username"]!! } + "me"
  }

  suspend fun completePath(path: String): UrlList {
    when {
      path == "/" -> {
        return UrlList(UrlList.Match.EXACT,
                (topLevel() + VERSION).map(addPath("https://graph.facebook.com/")))
      }
      path.matches("/v\\d.\\d+/?".toRegex()) -> {
        return UrlList(UrlList.Match.EXACT,
                topLevel().map(addPath("https://graph.facebook.com" + path)))
      }
      else -> {
        val prefix = "https://graph.facebook.com" + path

        val metadata = FacebookUtil.getMetadata(client, HttpUrl.parse(prefix)!!)

        return try {
          if (metadata == null) {
            UrlList(UrlList.Match.EXACT, listOf())
          } else {
            UrlList(UrlList.Match.EXACT, metadata.connections.keys.map(addPath(prefix)) + prefix)
          }
        } catch (e: Exception) {
          logger.log(Level.FINE, "completion failure", e)
          UrlList(UrlList.Match.EXACT, listOf())
        }
      }
    }
  }

  companion object {
    private val logger = Logger.getLogger(FacebookCompleter::class.java.name)
  }
}
