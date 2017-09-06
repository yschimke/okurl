package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.google.common.base.Throwables
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.ExecutionException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

import java.util.stream.Collectors.joining

class FacebookApiDocPresenter(private val sd: ServiceDefinition<Oauth2Token>) : ApiDocPresenter {

    @Throws(IOException::class)
    override fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient) {
        outputHandler.info("service: " + sd.shortName())
        outputHandler.info("name: " + sd.serviceName())
        sd.apiDocs().ifPresent { d -> outputHandler.info("docs: " + d) }
        sd.accountsLink().ifPresent { d -> outputHandler.info("apps: " + d) }

        try {
            val md = FacebookUtil.getMetadata(client, HttpUrl.parse(url)).get()
            outputHandler.info("")
            outputHandler.info("fields: " + md.fieldNames().stream().collect<String, *>(joining(",")))
            outputHandler.info("")
            outputHandler.info("connections: " + md.connections().stream().collect<String, *>(joining(",")))
        } catch (e: InterruptedException) {
            throw InterruptedIOException().initCause(e) as InterruptedIOException
        } catch (e: ExecutionException) {
            Throwables.propagateIfPossible(e.cause, IOException::class.java)
            throw Throwables.propagate(e.cause)
        }

    }
}
