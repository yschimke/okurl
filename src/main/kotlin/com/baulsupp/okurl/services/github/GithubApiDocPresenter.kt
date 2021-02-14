package com.baulsupp.okurl.services.github

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.openapi.OpenApiDocPresenter
import com.baulsupp.okurl.services.github.RequestDetails.Companion.reposRegex
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response

class GithubApiDocPresenter(client: OkHttpClient): ApiDocPresenter {
  val openApiDocPresenter = OpenApiDocPresenter(
    "https://github.com/github/rest-api-description/blob/main/descriptions/api.github.com/api.github.com.yaml?raw=true".toHttpUrl(),
    client
  )

  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val repoMatch = reposRegex.matchEntire(url)
    val url = if (repoMatch != null) {
      "https://api.github.com/repos/{owner}/{repo}/" + repoMatch.groupValues[3]
    } else {
      url
    }

    return openApiDocPresenter.explainApi(url, outputHandler, client, tokenSet)
  }
}
