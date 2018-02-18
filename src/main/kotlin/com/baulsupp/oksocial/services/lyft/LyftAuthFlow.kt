package com.baulsupp.oksocial.services.lyft

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URLEncoder

object LyftAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl = "https://api.lyft.com/oauth/authorize?client_id=$clientId&response_type=code&scope=$scopesString&state=x"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCodeAsync()

      val body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"authorization_code\", \"code\": \"$code\"}")
      val basic = Credentials.basic(clientId, clientSecret)
      val request = Request.Builder().url("https://api.lyft.com/oauth/token")
        .post(body)
        .header("Authorization", basic)
        .build()

      val responseMap = client.queryMap<String>(request)

      return Oauth2Token(responseMap["access_token"] as String,
        responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
