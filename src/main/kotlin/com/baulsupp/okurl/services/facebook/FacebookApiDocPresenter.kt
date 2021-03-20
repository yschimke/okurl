package com.baulsupp.okurl.services.facebook

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.credentials.Token
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Response

class FacebookApiDocPresenter(private val sd: ServiceDefinition<Oauth2Token>) : ApiDocPresenter {

  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    outputHandler.info("service: " + sd.shortName())
    outputHandler.info("name: " + sd.serviceName())
    sd.apiDocs()?.let { outputHandler.info("docs: $it") }
    sd.accountsLink()?.let { outputHandler.info("apps: $it") }

    val parsedUrl = url.toHttpUrlOrNull()

    if (parsedUrl != null) {
      val md = getMetadata(client, parsedUrl, tokenSet)

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
}
