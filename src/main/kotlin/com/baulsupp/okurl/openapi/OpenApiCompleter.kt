package com.baulsupp.okurl.openapi

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.flatMapMe
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.request
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.core.models.ParseOptions
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class OpenApiCompleter(
  val apiDesc: HttpUrl,
  val client: OkHttpClient
) : ApiCompleter {
  override suspend fun prefixUrls(): UrlList {
    val openAPI = readOpenAPI(client, apiDesc)

    val urls = openAPI?.servers?.map { it.url }
      .orEmpty()

    return UrlList(UrlList.Match.HOSTS, urls)
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    val openAPI = readOpenAPI(client, apiDesc)

    val urls = openAPI?.servers?.flatMapMe { server ->
      openAPI.paths.map { url ->
        server.url + "" + url.key
      }
    }
      .orEmpty()
    return UrlList(UrlList.Match.HOSTS, urls)
  }
}
