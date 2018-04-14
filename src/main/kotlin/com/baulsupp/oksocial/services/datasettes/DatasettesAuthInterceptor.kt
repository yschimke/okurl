package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.services.datasettes.model.DatasetteIndex
import com.baulsupp.oksocial.services.datasettes.model.DatasetteTables
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * https://datasettes.com/
 */
class DatasettesAuthInterceptor :
  CompletionOnlyAuthInterceptor("datasettes.com", "Datasettes", "datasettes",
    "https://github.com/simonw/datasette") {
  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter =
    DatasettesCompleter(client)

  override fun hosts(): Set<String> = knownHosts()

  override fun apiDocPresenter(url: String): ApiDocPresenter = DatasettesPresenter()
}

class DatasettesCompleter(private val client: OkHttpClient) : ApiCompleter {
  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    val host = url.host()

    val path = url.pathSegments()

    return when {
      path.size == 1 -> {
        val datasette = fetchDatasetteMetadata(host, client)
        databaseInPath(datasette, host)
      }
      path.size == 2 -> {
        val datasetteTables = runBlocking { fetchDatasetteTableMetadata(host, path.first(), client) }
        tablesInDatabase(datasetteTables, host, path)
      }
      else -> UrlList(UrlList.Match.EXACT, listOf())
    }
  }

  private fun databaseInPath(datasette: List<DatasetteIndex>, host: String?): UrlList {
    val paths = datasette.flatMap { listOf(it.path + ".json", it.path + "/") } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/$it" })
  }

  private fun tablesInDatabase(
    datasetteTables: DatasetteTables,
    host: String?,
    path: MutableList<String>
  ): UrlList {
    val tableLike = datasetteTables.views + datasetteTables.tables.map { it.name }
    val encoded = tableLike.map { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
    val paths = encoded.map { "$it.json" } + ".json"
    return UrlList(UrlList.Match.EXACT, paths.map { "https://$host/${path.first()}/$it" })
  }

  override suspend fun prefixUrls(): UrlList =
    UrlList(UrlList.Match.HOSTS, knownHosts().map { "https://$it/" })
}

class DatasettesPresenter : ApiDocPresenter {
  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val urlI = HttpUrl.parse(url) ?: throw UsageException("Unable to parse Url '$url'")

    val datasettes = runBlocking { fetchDatasetteMetadata(urlI.host(), client) }

    if (datasettes.size != 1) {
      outputHandler.showError("expected 1 datasette: '${datasettes.joinToString { it.path }}'")
    }

    val datasette = datasettes.first()

    outputHandler.info("service: Datasette")
    outputHandler.info("name: ${datasette.name}")
    outputHandler.info("docs: https://github.com/simonw/datasette")
    outputHandler.info("database: https://${urlI.host()}/${datasette.path}")
    outputHandler.info("")

    val datasetteTables = runBlocking {
      fetchDatasetteTableMetadata(urlI.host(), datasette.path, client)
    }

    outputHandler.info("tables: " + datasetteTables.tables.joinToString { it.name })
    outputHandler.info("views: " + datasetteTables.views.joinToString())
  }
}

suspend fun fetchDatasetteMetadata(host: String, client: OkHttpClient) =
  client.queryMap<DatasetteIndex>("https://$host/.json", NoToken).values.toList()

suspend fun fetchDatasetteTableMetadata(
  host: String,
  path: String,
  client: OkHttpClient
) =
  client.query<DatasetteTables>("https://$host/$path.json",
    NoToken)

fun knownHosts(): Set<String> =
  DatasettesAuthInterceptor::class.java.getResource("/datasettes.txt")?.readText()?.split('\n')?.toSet() ?: setOf()
