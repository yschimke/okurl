package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.google.common.base.Throwables
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors.joining

class FacebookApiDocPresenter(private val sd: ServiceDefinition<Oauth2Token>) : ApiDocPresenter {

    @Throws(IOException::class)
    override fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient) {
        outputHandler.info("service: " + sd.shortName())
        outputHandler.info("name: " + sd.serviceName())
        sd.apiDocs().ifPresent { d -> outputHandler.info("docs: " + d) }
        sd.accountsLink().ifPresent { d -> outputHandler.info("apps: " + d) }

        val parsedUrl = HttpUrl.parse(url)
        // TODO handle null
        val md = FacebookUtil.getMetadata(client, parsedUrl!!).get()
        outputHandler.info("")
        outputHandler.info("fields: " + md.fieldNames().joinToString(","))
        outputHandler.info("")
        outputHandler.info("connections: " + md.connections().joinToString(","))
    }
}
