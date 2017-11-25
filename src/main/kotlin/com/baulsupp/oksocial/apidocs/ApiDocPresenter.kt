package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response

interface ApiDocPresenter {
  suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient)

  companion object {
    val NONE = object : ApiDocPresenter {
      override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) =
          outputHandler.info("No documentation for: " + url)
    }
  }
}
