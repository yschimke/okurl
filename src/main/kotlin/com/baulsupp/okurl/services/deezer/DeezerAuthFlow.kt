package com.baulsupp.okurl.services.deezer

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryForString
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object DeezerAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    appId: String,
    secretKey: String,
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val perms = listOf(
        "basic_access",
        "email",
        "offline_access",
        "manage_library",
        "manage_community",
        "delete_library",
        "listening_history"
      )
      val loginUrl =
        "https://connect.deezer.com/oauth/auth.php?app_id=$appId&redirect_uri=${s.redirectUri}&perms=${
          perms.joinToString(
            ","
          )
        }"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val response = client.queryForString("https://connect.deezer.com/oauth/access_token.php?app_id=$appId&secret=$secretKey&code=$code")

      val params = response.split("&").map {
        val (key, value) = it.split("=", limit = 2)
        key to value
      }.toMap()

      val accessToken = params["access_token"]!!

      return Oauth2Token(accessToken)
    }
  }
}
