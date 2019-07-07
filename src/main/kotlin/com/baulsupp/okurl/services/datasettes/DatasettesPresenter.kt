package com.baulsupp.okurl.services.datasettes

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.services.datasettes.model.DatasetteIndex2
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Response

class DatasettesPresenter : ApiDocPresenter {
  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val urlI = url.toHttpUrlOrNull()
      ?: throw UsageException("Unable to parse Url '$url'")

    val datasette = client.queryMap<DatasetteIndex2>("https://${urlI.host}/-/inspect.json", NoToken)

    outputHandler.info("service: Datasette")
    outputHandler.info("docs: https://github.com/simonw/datasette")
    outputHandler.info("database: https://${urlI.host}/")

    datasette.forEach { (db, dbi) ->
      outputHandler.info("")
      outputHandler.info("name: $db")
      outputHandler.info("tables: " + dbi.tables.keys.joinToString())
//    outputHandler.info("views: " + datasette.views.joinToString())
    }
  }
}
