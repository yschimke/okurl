package com.baulsupp.okurl.openapi

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.flatMapMe
import io.swagger.v3.oas.models.OpenAPI
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class OpenApiCompleter(
  val loader: suspend () -> OpenAPI?
) : ApiCompleter {
  constructor(
    apiDesc: HttpUrl,
    client: OkHttpClient): this({ readOpenAPI(client, apiDesc) })

  override suspend fun prefixUrls(): UrlList {
    val openAPI = loader()

    val urls = openAPI?.servers?.map { it.url }
      .orEmpty()

    return UrlList(UrlList.Match.HOSTS, urls)
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    val openAPI = loader()

    val urls = openAPI?.servers?.flatMapMe { server ->
      openAPI.paths.map { url ->
        server.url + "" + url.key
      }
    }.orEmpty()

    return UrlList(UrlList.Match.HOSTS, urls)
  }
}
