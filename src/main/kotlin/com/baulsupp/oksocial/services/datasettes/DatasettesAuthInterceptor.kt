package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import com.squareup.moshi.Json
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * https://datasettes.com/
 */
class DatasettesAuthInterceptor : CompletionOnlyAuthInterceptor("datasettes.com", "Datasettes", "datasettes", "https://github.com/simonw/datasette") {
  override fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter =
      DatasettesCompleter(client)

  override fun hosts(): Set<String> = knownHosts()

  override fun apiDocPresenter(url: String): ApiDocPresenter = DatasettesPresenter()
}

class DatasettesCompleter(private val client: OkHttpClient) : ApiCompleter {
  override suspend fun siteUrls(url: HttpUrl): UrlList {
    val host = url.host()

    val path = url.pathSegments()

    if (path.size == 1) {
      val datasette = runBlocking { fetchDatasetteMetadata(host, client) }
      return databaseInPath(datasette, host)
    } else if (path.size == 2) {
      val datasetteTables = runBlocking { fetchDatasetteTableMetadata(host, path.first(), client) }
      return tablesInDatabase(datasetteTables, host, path)
    } else {
      return UrlList(UrlList.Match.EXACT, listOf())
    }
  }

  private fun databaseInPath(datasette: List<DatasetteIndex>, host: String?): UrlList {
    val paths = datasette.flatMap { listOf(it.path + ".json", it.path + "/") } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/$it" })
  }

  private fun tablesInDatabase(datasetteTables: DatasetteTables, host: String?, path: MutableList<String>): UrlList {
    val tableLike = datasetteTables.views + datasetteTables.tables.map { it.name }
    val encoded = tableLike.map { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
    val paths = encoded.map { "$it.json" } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/${path.first()}/$it" })
  }

  suspend override fun prefixUrls(): UrlList =
      UrlList(UrlList.Match.HOSTS, knownHosts().map { "https://$it/" })
}

class DatasettesPresenter: ApiDocPresenter {
  override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
    val urlI = HttpUrl.parse(url) ?: throw UsageException("Unable to parse Url '$url'")

    val datasettes = runBlocking { fetchDatasetteMetadata(urlI.host(), client) }

    if (datasettes.size != 1) {
      outputHandler.showError("expected 1 datasette: '${datasettes.map { it.path }.joinToString()}'", null)
    }

    val datasette = datasettes.first()

    outputHandler.info("service: Datasette")
    outputHandler.info("name: ${datasette.name}")
    outputHandler.info("docs: https://github.com/simonw/datasette")
    outputHandler.info("database: https://${urlI.host()}/${datasette.path}")
    outputHandler.info("")

    val datasetteTables = runBlocking { fetchDatasetteTableMetadata(urlI.host(), datasette.path, client) }

    outputHandler.info("tables: " + datasetteTables.tables.map { it.name }.joinToString())
    outputHandler.info("views: " + datasetteTables.views.joinToString())
  }
}

data class DatasetteIndex(val name: String, val hash: String, val path: String, @Json(name="tables_truncated") val tables: List<Any>)

data class DatasetteTable(val name: String, val columns: List<String>, @Json(name="table_rows") val tableRows: Int)

data class DatasetteTables(val database: String, val tables: List<DatasetteTable>, val views: List<String>, val source: String, @Json(name="source_url") val sourceUrl: String)

suspend fun fetchDatasetteMetadata(host: String, client: OkHttpClient): List<DatasetteIndex> =
    client.queryMap<String, DatasetteIndex>("https://$host/.json").values.toList()

suspend fun fetchDatasetteTableMetadata(host: String, path: String, client: OkHttpClient): DatasetteTables =
    client.query<DatasetteTables>("https://$host/$path.json")

fun knownHosts(): Set<String> = setOf("fivethirtyeight.datasettes.com", "parlgov.datasettes.com")
