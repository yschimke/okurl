package com.baulsupp.okurl.openapi

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.request
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class OpenApiCompleter(val apiDesc: HttpUrl, val client: OkHttpClient): ApiCompleter {
  override suspend fun prefixUrls(): UrlList {
    val openAPI = openAPI()

    val urls = openAPI?.servers?.map { it.url }.orEmpty()

    return UrlList(UrlList.Match.HOSTS, urls)
  }

  private suspend fun openAPI(): OpenAPI? {
    val yaml = client.queryForString(apiDesc.request())
    return OpenAPIV3Parser().readContents(yaml).openAPI
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    val openAPI = openAPI()
    val urls = openAPI?.servers?.flatMap { server ->
      openAPI.paths.map {url ->
        server.url + "" + url.key
      }
    }.orEmpty()
    return UrlList(UrlList.Match.HOSTS, urls)
  }
}
