package com.baulsupp.okurl.apidocs

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.FileUtil
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File

class ServiceApiDocPresenter(private val services: AuthenticatingInterceptor) : ApiDocPresenter {
  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val cachedClient = client.newBuilder()
      .cache(ApiCompleter.cache)
      .build()

    val presenter = services.getByUrl(url)?.apiDocPresenter(url, cachedClient)

    if (presenter != null) {
      presenter.explainApi(url, outputHandler, cachedClient, tokenSet)
    } else {
      outputHandler.info("No documentation for: $url")
    }
  }
}
