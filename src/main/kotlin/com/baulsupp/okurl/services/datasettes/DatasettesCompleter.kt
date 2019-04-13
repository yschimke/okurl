package com.baulsupp.okurl.services.datasettes

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.services.datasettes.model.DatasetteIndex2
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DatasettesCompleter(private val client: OkHttpClient) : ApiCompleter {
  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    val host = url.host()

    val path = url.pathSegments()

    return when {
      path.size == 1 -> {
        val datasette = client.queryMap<DatasetteIndex2>("https://$host/-/inspect.json", NoToken)
        databaseInPath(datasette, host)
      }
      path.size == 2 -> {
        val datasette = client.queryMap<DatasetteIndex2>("https://$host/-/inspect.json", NoToken)
        tablesInDatabase(datasette, host, path)
      }
      else -> UrlList(UrlList.Match.EXACT, listOf())
    }
  }

  private fun databaseInPath(datasette: Map<String, DatasetteIndex2>, host: String): UrlList {
    val paths = datasette.keys.flatMap { listOf("$it.json", "$it/") } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/$it" })
  }

  private fun tablesInDatabase(
    datasettes: Map<String, DatasetteIndex2>,
    host: String?,
    path: List<String>
  ): UrlList {
    val db = path.first().replace("-[0-9a-z]+".toRegex(), "")
    val datasette = datasettes.getValue(db)
    val tableLike = datasette.views + datasette.tables.map { it.key }
    val encoded = tableLike.map { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
    val paths = encoded.map { "$it.json" } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/${path.first()}/$it" })
  }

  override suspend fun prefixUrls(): UrlList =
    UrlList(UrlList.Match.HOSTS, knownHosts().map { "https://$it/" })
}
