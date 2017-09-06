package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import okhttp3.OkHttpClient

interface ApiDocPresenter {

    @Throws(IOException::class)
    fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient)

    companion object {
        val NONE = { url, outputHandler, client -> outputHandler.info("No documentation for: " + url) }
    }
}
