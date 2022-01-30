package com.baulsupp.okurl.services.foursquare

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder

object FourSquareAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val serverUri = s.redirectUri

      val loginUrl = "https://foursquare.com/oauth2/authenticate?client_id=$clientId&redirect_uri=${URLEncoder.encode(
        serverUri, "UTF-8"
      )}&response_type=code"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl =
        "https://foursquare.com/oauth2/access_token?client_id=$clientId&client_secret=$clientSecret&grant_type=authorization_code&redirect_uri=${URLEncoder.encode(
          serverUri, "UTF-8"
        )}&code=$code"

      val responseMap = client.queryMap<Any>(tokenUrl, NoToken)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
