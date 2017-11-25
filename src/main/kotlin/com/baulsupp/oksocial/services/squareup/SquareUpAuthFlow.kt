package com.baulsupp.oksocial.services.squareup

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.JsonUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URLEncoder
import java.util.HashMap

object SquareUpAuthFlow {

  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val serverUri = s.redirectUri

      val loginUrl = "https://connect.squareup.com/oauth2/authorize?client_id=$clientId&redirect_uri=${URLEncoder.encode(serverUri, "UTF-8")}&response_type=code&scope=" + URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://connect.squareup.com/oauth2/token"
      val map = HashMap<String, String>()
      map.put("client_id", clientId)
      map.put("client_secret", clientSecret)
      map.put("code", code)
      map.put("redirect_uri", serverUri)
      val body = JsonUtil.toJson(map)

      val reqBody = RequestBody.create(MediaType.parse("application/json"), body)
      val request = Request.Builder().url(tokenUrl).post(reqBody).build()
      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
