package com.baulsupp.oksocial.services.squareup

import com.baulsupp.oksocial.NoToken
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2TokenResponse
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.JsonUtil
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URLEncoder.encode

object SquareUpAuthFlow {

  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://connect.squareup.com/oauth2/authorize?client_id=$clientId&redirect_uri=${encode(
        s.redirectUri, "UTF-8")}&response_type=code&scope=${encode(
        scopes.joinToString(" "), "UTF-8")}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://connect.squareup.com/oauth2/token"
      val map = mapOf("client_id" to clientId, "client_secret" to clientSecret,
        "code" to code, "redirect_uri" to s.redirectUri)

      val reqBody = RequestBody.create(MediaType.parse("application/json"), JsonUtil.toJson(map))
      val request = Request.Builder().url(tokenUrl).post(reqBody).build()

      val response = client.query<Oauth2TokenResponse>(request, NoToken)

      return Oauth2Token(response.access_token)
    }
  }
}
