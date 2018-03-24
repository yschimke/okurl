package com.baulsupp.oksocial.services.squareup

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2TokenResponse
import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.kotlin.postJsonBody
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.services.squareup.model.AuthDetails
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder.encode

object SquareUpAuthFlow {

  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val loginUrl = "https://connect.squareup.com/oauth2/authorize?client_id=$clientId&redirect_uri=${encode(s.redirectUri, "UTF-8")}&response_type=code&scope=${encode(scopes.joinToString(" "), "UTF-8")}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val response = client.query<Oauth2TokenResponse>(request("https://connect.squareup.com/oauth2/token", NoToken) {
        postJsonBody(AuthDetails(clientId, clientSecret, code, s.redirectUri))
      })

      return Oauth2Token(response.access_token)
    }
  }
}
