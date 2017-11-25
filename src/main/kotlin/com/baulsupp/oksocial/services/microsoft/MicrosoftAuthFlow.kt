package com.baulsupp.oksocial.services.microsoft

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object MicrosoftAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
            clientSecret: String): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://login.microsoftonline.com/common/oauth2/authorize?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val url = HttpUrl.parse("https://login.microsoftonline.com/common/oauth2/token")

      val body = FormBody.Builder().add("grant_type", "authorization_code")
          .add("redirect_uri", s.redirectUri)
          .add("client_id", clientId)
          .add("client_secret", clientSecret)
          .add("code", code)
          .add("resource", "https://graph.microsoft.com/")
          .build()

      val request = Request.Builder().url(url!!).post(body).build()

      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String,
          responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
