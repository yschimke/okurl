package com.baulsupp.okurl.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.authenticator.AuthenticatingInterceptor
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
    val client = client.newBuilder()
      .cache(Cache(File(FileUtil.okurlSettingsDir, "completion-cache"), 256 * 1024 * 1024))
      .build()

    val presenter = services.getByUrl(url)?.apiDocPresenter(url, client)

    if (presenter != null) {
      presenter.explainApi(url, outputHandler, client, tokenSet)
    } else {
      outputHandler.info("No documentation for: $url")
    }
  }
}
