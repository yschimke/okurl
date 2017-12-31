package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response

class ServiceApiDocPresenter(private val services: ServiceInterceptor) : ApiDocPresenter {
  suspend override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
    val presenter = services.getByUrl(url)?.apiDocPresenter(url)

    if (presenter != null) {
      presenter.explainApi(url, outputHandler, client)
    } else {
      outputHandler.info("No documentation for: " + url)
    }
  }
}
