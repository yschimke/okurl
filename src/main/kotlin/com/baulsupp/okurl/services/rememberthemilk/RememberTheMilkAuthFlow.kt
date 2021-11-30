package com.baulsupp.okurl.services.rememberthemilk

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.oksocial.output.readString
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.kotlin.queryForString
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.ByteString.Companion.encodeUtf8
import org.w3c.dom.Document
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory


object RememberTheMilkAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    apiKey: String,
    apiSecret: String
  ): RememberTheMilkCredentials {
    SimpleWebServer { r ->
      r.queryParameter("frob")
    }.use { s ->
      val frobUrl =
        "https://api.rememberthemilk.com/services/rest/?method=rtm.auth.getFrob&api_key=${apiKey}"
      val frobResponse = client.queryForString(frobUrl)
      val fromDoc = frobResponse.parseXml()

      val frob = fromDoc.getElementsByTagName("frob").item(0)?.textContent
        ?: throw IOException("Failed to get frob: $frobResponse")

      val sig = "${apiSecret}api_key${apiKey}frob${frob}permsdelete".encodeUtf8().md5().hex()

      val loginUrl =
        "https://www.rememberthemilk.com/services/auth/?api_key=${apiKey}&perms=delete&frob=$frob&api_sig=$sig"

      outputHandler.openLink(loginUrl)

      System.console().readString("Enter after authentication: ")

      val url =
        "https://api.rememberthemilk.com/services/rest/?method=rtm.auth.getToken&api_key=${apiKey}&frob=$frob"
      val response = client.queryForString(url)

      val document = response.parseXml()

      val token = document.getElementsByTagName("token").item(0)?.textContent
          ?: throw IOException("Failed to login: $response")

      return RememberTheMilkCredentials(apiKey, apiSecret, token)
    }
  }
}

private fun String.parseXml(): Document {
  val dbf = DocumentBuilderFactory.newInstance()
  val db = dbf.newDocumentBuilder()
  return db.parse(this.byteInputStream())
}
