package com.baulsupp.okurl.services.github

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.openapi.OpenApiCompleter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class GithubApiCompleter(client: OkHttpClient) : ApiCompleter {
  val client = client.newBuilder()
    .addNetworkInterceptor {
      val response = it.proceed(it.request())
      response.newBuilder()
        .header("Cache-Control", "max-age=3600")
        .build()
    }
    .build()

  override suspend fun prefixUrls(): UrlList {
    return UrlList.hosts("api.github.com", "uploads.github.com")
  }

  override suspend fun siteUrls(
    url: HttpUrl,
    tokenSet: Token
  ): UrlList {
    val completer = OpenApiCompleter(
      "https://raw.githubusercontent.com/github/rest-api-description/main/descriptions/api.github.com/api.github.com.yaml".toHttpUrl(),
      client
    )

    return completer.siteUrls(url, DefaultToken)
  }
}
