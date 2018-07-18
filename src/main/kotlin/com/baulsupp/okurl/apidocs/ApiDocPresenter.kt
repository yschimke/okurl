package com.baulsupp.okurl.apidocs

import com.baulsupp.okurl.credentials.Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response

interface ApiDocPresenter {
  suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token)

  companion object {
    val NONE = object : ApiDocPresenter {
      override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient, tokenSet: Token) =
        outputHandler.info("No documentation for: $url")
    }
  }
}
