package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.services.facebook.FacebookUtil.VERSION
import com.baulsupp.oksocial.services.facebook.model.Account
import com.baulsupp.oksocial.services.facebook.model.AccountList
import com.baulsupp.oksocial.services.facebook.model.UserOrPage
import com.baulsupp.oksocial.util.ClientException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger

class FacebookCompleter(private val client: OkHttpClient, hosts: Collection<String>) :
  HostUrlCompleter(hosts) {

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    if (url.host() == "www.facebook.com") {
      return UrlList.fromResource("facebook")!!
    }

    var result = completePath(url.encodedPath(), tokenSet)

    if (!url.encodedPath().endsWith("/")) {
      val parentPaths = url.encodedPathSegments()
      parentPaths.removeAt(parentPaths.size - 1)

      val parentPath = "/" + parentPaths.joinToString("/")

      result = result.combine(completePath(parentPath, tokenSet))
    }

    return result
  }

  private fun addPath(prefix: String): (String) -> String {
    return { c: String -> prefix + (if (prefix.endsWith("/")) "" else "/") + c }
  }

  suspend fun topLevel(tokenSet: Token): List<String> {
    val topLevel = mutableListOf("me")

    if (isWorkplace(tokenSet)) {
      topLevel.add("community")
    } else {
      topLevel += listAccounts(tokenSet)
    }

    return topLevel
  }

  private suspend fun listAccounts(tokenSet: Token): List<String> = try {
    client.fbQueryList<Account, AccountList>("/me/accounts", tokenSet).data.map { it.username ?: it.id }
  } catch (ce: ClientException) {
    if (ce.code != 400) {
      logger.log(Level.FINE, "Failed to load accounts", ce)
    }
    listOf()
  }

  private suspend fun isWorkplace(tokenSet: Token): Boolean = try {
    client.fbQuery<UserOrPage>("/community", tokenSet)
    true
  } catch (ce: ClientException) {
    if (ce.code != 400) {
      logger.log(Level.FINE, "Failed to load accounts", ce)
    }
    false
  }

  suspend fun completePath(path: String, tokenSet: Token): UrlList {
    when {
      path == "/" -> {
        return UrlList(UrlList.Match.EXACT,
          (topLevel(tokenSet) + VERSION).map(addPath("https://graph.facebook.com/")))
      }
      path.matches("/v\\d.\\d+/?".toRegex()) -> {
        return UrlList(UrlList.Match.EXACT,
          topLevel(tokenSet).map(addPath("https://graph.facebook.com$path")))
      }
      else -> {
        val prefix = "https://graph.facebook.com$path"

        val metadata = FacebookUtil.getMetadata(client, HttpUrl.parse(prefix)!!, tokenSet)

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
