package com.baulsupp.okurl.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
import com.baulsupp.okurl.credentials.Token
import okhttp3.OkHttpClient
import okhttp3.Response

class ServiceApiDocPresenter(private val services: AuthenticatingInterceptor) : ApiDocPresenter {
  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val presenter = services.getByUrl(url)?.apiDocPresenter(url, client)

    if (presenter != null) {
      presenter.explainApi(url, outputHandler, client, tokenSet)
    } else {
      outputHandler.info("No documentation for: $url")
    }
  }
}
