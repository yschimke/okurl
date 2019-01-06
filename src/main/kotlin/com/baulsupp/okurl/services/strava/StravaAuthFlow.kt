package com.baulsupp.okurl.services.strava

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import okhttp3.OkHttpClient
import okhttp3.Response

data class AuthResponse(
  val token_type: String,
  val access_token: String,
  val athlete: Map<String, Any>,
  val refresh_token: String,
  val expires_at: Long,
  val state: String? = null
)

object StravaAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val loginUrl =
        "https://www.strava.com/oauth/authorize?client_id=$clientId&redirect_uri=${s.redirectUri}&response_type=code&scope=${scopes.joinToString(
          ","
        )}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val responseMap =
        client.query<AuthResponse>(com.baulsupp.okurl.kotlin.request("https://www.strava.com/oauth/token") {
          post(
            form {
              add("grant_type", "authorization_code")
              add("client_id", clientId)
              add("client_secret", clientSecret)
              add("code", code)
            }
          )
        })

      return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
    }
  }
}
