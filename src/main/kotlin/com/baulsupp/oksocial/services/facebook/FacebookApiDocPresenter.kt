package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response

class FacebookApiDocPresenter(private val sd: ServiceDefinition<Oauth2Token>) : ApiDocPresenter {

  override suspend fun explainApi(url: String, outputHandler: OutputHandler<Response>,
                                  client: OkHttpClient) {
    outputHandler.info("service: " + sd.shortName())
    outputHandler.info("name: " + sd.serviceName())
    sd.apiDocs()?.let { outputHandler.info("docs: " + it) }
    sd.accountsLink()?.let { outputHandler.info("apps: " + it) }

    val parsedUrl = HttpUrl.parse(url)
    // TODO handle null
    val md = FacebookUtil.getMetadata(client, parsedUrl!!)
    if (md == null) {
      outputHandler.info("")
      outputHandler.info("No metadata available")
    } else {
      outputHandler.info("")
      outputHandler.info("fields: " + md.fields.joinToString(",") { it.name })
      outputHandler.info("")
      outputHandler.info("connections: " + md.connections.keys.joinToString(","))
    }
  }
}
