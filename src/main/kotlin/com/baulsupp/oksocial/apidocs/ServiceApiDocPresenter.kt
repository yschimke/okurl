package com.baulsupp.oksocial.apidocs

import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.google.common.base.Throwables
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Optional
import okhttp3.OkHttpClient

class ServiceApiDocPresenter(private val services: ServiceInterceptor, private val client: OkHttpClient,
                             private val credentialsStore: CredentialsStore) : ApiDocPresenter {

    @Throws(IOException::class)
    override fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient) {
        val presenter = services.getByUrl(url).map<ApiDocPresenter> { s ->
            try {
                return@services.getByUrl(url).map s . apiDocPresenter url
            } catch (e: IOException) {
                throw Throwables.propagate(e)
            }
        }

        if (presenter.isPresent) {
            presenter.get().explainApi(url, outputHandler, client)
        } else {
            outputHandler.info("No documentation for: " + url)
        }
    }
}
