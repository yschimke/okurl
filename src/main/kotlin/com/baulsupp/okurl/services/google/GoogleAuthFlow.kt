package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.UUID

object GoogleAuthFlow {
  fun fullScope(suffix: String): String {
    return if (suffix.contains("/")) suffix else "https://www.googleapis.com/auth/$suffix"
  }

  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val scopesString = scopes.joinToString("+", transform = { fullScope(it) })

      val redirectUri = s.redirectUri
      val uuid = UUID.randomUUID().toString()

      val loginUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=$clientId&response_type=code&scope=$scopesString&state=$uuid&access_type=offline&redirect_uri=$redirectUri&prompt=consent&include_granted_scopes=true"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://www.googleapis.com/oauth2/v4/token"
      val body = FormBody.Builder().add("client_id", clientId)
        .add("redirect_uri", redirectUri)
        .add("client_secret", clientSecret)
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()
      val request = Request.Builder().url(tokenUrl).method("POST", body).build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(responseMap["access_token"] as String,
        responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
