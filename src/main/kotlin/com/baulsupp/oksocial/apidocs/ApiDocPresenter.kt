package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

interface ApiDocPresenter {

  @Throws(IOException::class)
  fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient)

  companion object {
    val NONE = object : ApiDocPresenter {
      override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) =
          outputHandler.info("No documentation for: " + url)
    }
  }
}
