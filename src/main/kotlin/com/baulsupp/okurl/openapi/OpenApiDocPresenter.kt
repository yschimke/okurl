package com.baulsupp.okurl.openapi

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.request
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.core.models.ParseOptions
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.ExperimentalFileSystem
import okio.FileSystem
import okio.Path

suspend fun readOpenAPI(
  client: OkHttpClient,
  apiDesc: HttpUrl
): OpenAPI? {
  val yaml = client.queryForString(apiDesc.request())

  val options = ParseOptions().apply {
    isResolve = true
  }
  val readContents = OpenAPIParser().readContents(yaml, null, options)
  return readContents.openAPI
}

@OptIn(ExperimentalFileSystem::class)
suspend fun readOpenAPI(
  path: Path,
  fileSystem: FileSystem
): OpenAPI? {
  fileSystem.read(path) {
    val options = ParseOptions().apply {
      isResolve = true
    }
    val readContents = OpenAPIParser().readContents(readUtf8(), null, options)
    return readContents.openAPI
  }
}

class OpenApiDocPresenter(
  val loader: suspend () -> OpenAPI?
) : ApiDocPresenter {
  constructor(
    apiDesc: HttpUrl,
    client: OkHttpClient): this({ readOpenAPI(client, apiDesc) })

  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val openAPI = loader()

    if (openAPI != null) {
      val server = openAPI.servers.find { url.startsWith(it.url) }

      if (server != null) {
        val paths = openAPI.paths
        val urlPath = url.drop(server.url.length)

        // TODO improve search
        val path = paths?.get(urlPath)?.get

        if (path != null) {
          outputHandler.info("Description: " + path.description)
          outputHandler.info("Docs: " + path.externalDocs?.url)

          if (!path.parameters.isNullOrEmpty()) {
            outputHandler.info("")

            outputHandler.info("Parameters")
            path.parameters.forEach {
              outputHandler.info(it.name + " " + it.description)
              outputHandler.info("")
            }
          }
          return
        }
      }
    }
  }
}
