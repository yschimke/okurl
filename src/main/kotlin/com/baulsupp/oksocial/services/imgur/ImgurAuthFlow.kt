package com.baulsupp.oksocial.services.imgur

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object ImgurAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=code&state=x"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("client_id", clientId)
          .add("client_secret", clientSecret)
          .add("code", code)
          .add("grant_type", "authorization_code")
          .build()
      val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
          .method("POST", body)
          .build()

      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String,
          responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
