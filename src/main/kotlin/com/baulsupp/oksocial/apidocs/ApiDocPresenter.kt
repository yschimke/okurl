package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import java.io.IOException

@FunctionalInterface
interface ApiDocPresenter {

    @Throws(IOException::class)
    fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient): Unit

//    companion object {
//        val NONE = object: ApiDocPresenter {
//            override fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient) =
//                    outputHandler.info("No documentation for: " + url)
//        }
//    }
}
