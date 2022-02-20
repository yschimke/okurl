package com.baulsupp.okurl.services.weekdone

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.squareup.moshi.JsonClass
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response

@JsonClass(generateAdapter = true)
data class OauthTokenResponse(val access_token: String, val refresh_token: String)

object WeekdoneAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val clientId = Secrets.prompt("Weekdone Client ID", "weekdone.clientId", "", false)
      val clientSecret = Secrets.prompt("Weekdone Client Secret", "weekdone.clientSecret", "", true)

      val loginUrl =
        "https://weekdone.com/oauth_authorize?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val responseMap = client.query<OauthTokenResponse>(request("https://weekdone.com/oauth_token") {
        post(
          FormBody.Builder().add("code", code).add("grant_type", "authorization_code").add(
            "redirect_uri",
            s.redirectUri
          ).add("client_id", clientId).add("client_secret", clientSecret).build()
        )
      })

      return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
    }
  }
}
