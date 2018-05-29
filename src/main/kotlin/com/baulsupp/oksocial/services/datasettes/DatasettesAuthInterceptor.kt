package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.services.datasettes.model.DatasetteIndex2
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

  override fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter = DatasettesPresenter()
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
        val datasette = fetchDatasetteMetadata(host, client)
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
    path: MutableList<String>
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

class DatasettesPresenter : ApiDocPresenter {
  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val urlI = HttpUrl.parse(url) ?: throw UsageException("Unable to parse Url '$url'")

    val datasette = fetchDatasetteMetadata(urlI.host(), client)

    outputHandler.info("service: Datasette")
    outputHandler.info("docs: https://github.com/simonw/datasette")
    outputHandler.info("database: https://${urlI.host()}/")

    datasette.forEach { db, dbi ->
      outputHandler.info("")
      outputHandler.info("name: ${db}")
      outputHandler.info("tables: " + dbi.tables.keys.joinToString())
//    outputHandler.info("views: " + datasette.views.joinToString())
    }
  }
}

suspend fun fetchDatasetteMetadata(host: String, client: OkHttpClient): Map<String, DatasetteIndex2> {
  return client.queryMap<DatasetteIndex2>("https://$host/-/inspect.json", NoToken)
}

fun knownHosts(): Set<String> =
  DatasettesAuthInterceptor::class.java.getResource("/datasettes.txt")?.readText()?.split('\n')?.toSet() ?: setOf()
