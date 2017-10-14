package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class FacebookApiDocPresenter(private val sd: ServiceDefinition<Oauth2Token>) : ApiDocPresenter {

    @Throws(IOException::class)
    override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
        outputHandler.info("service: " + sd.shortName())
        outputHandler.info("name: " + sd.serviceName())
        sd.apiDocs()?.let { outputHandler.info("docs: " + it) }
        sd.accountsLink()?.let { outputHandler.info("apps: " + it) }

        val parsedUrl = HttpUrl.parse(url)
        // TODO handle null
        val md = FacebookUtil.getMetadata(client, parsedUrl!!).get()
        outputHandler.info("")
        outputHandler.info("fields: " + md.fieldNames().joinToString(","))
        outputHandler.info("")
        outputHandler.info("connections: " + md.connections().joinToString(","))
    }
}
