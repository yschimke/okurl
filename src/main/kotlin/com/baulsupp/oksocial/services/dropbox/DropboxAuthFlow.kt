package com.baulsupp.oksocial.services.dropbox

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object DropboxAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
            clientSecret: String): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://www.dropbox.com/1/oauth2/authorize?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val basic = Credentials.basic(clientId, clientSecret)
      val body = FormBody.Builder().add("code", code)
          .add("grant_type", "authorization_code")
          .add("redirect_uri", s.redirectUri)
          .build()
      val request = Request.Builder().url("https://api.dropboxapi.com/1/oauth2/token")
          .post(body)
          .header("Authorization", basic)
          .build()

      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
