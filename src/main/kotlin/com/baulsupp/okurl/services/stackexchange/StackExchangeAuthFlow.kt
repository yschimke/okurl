package com.baulsupp.okurl.services.stackexchange

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.requestBuilder
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder

object StackExchangeAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    clientKey: String,
    scopes: Iterable<String>
  ): StackExchangeToken {
    SimpleWebServer.forCode().use { s ->
      val serverUri = s.redirectUri

      val loginUrl =
        "https://stackexchange.com/oauth?client_id=$clientId&redirect_uri=$serverUri&scope=" + URLEncoder.encode(
          scopes.joinToString(","), "UTF-8"
        )

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://stackexchange.com/oauth/access_token"

      val body = FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
        .add("client_secret", clientSecret).add("code", code).build()
      val request = requestBuilder(tokenUrl, NoToken).method("POST", body).build()

      val longTokenBody = client.queryForString(request)

      return StackExchangeToken(parseExchangeRequest(longTokenBody), clientKey)
    }
  }

  private fun parseExchangeRequest(body: String): String? {
    val params = body.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    return params
      .map { p -> p.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
      .firstOrNull { it[0] == "access_token" }
      ?.let { it[1] }
  }
}
