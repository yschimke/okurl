package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.authflow.State
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.FormBody
import okhttp3.Request

class GoogleAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow<Oauth2Token>(
  serviceDefinition
) {
  fun fullScope(suffix: String): String {
    return if (suffix.contains("/")) suffix else "https://www.googleapis.com/auth/$suffix"
  }

  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("google.clientId", "Google Client Id", null, false),
      Prompt("google.clientSecret", "Google Client Secret", null, true),
      Scopes("google.scopes", "Scopes", known = listOf("plus.login", "plus.profile.emails.read")),
      Callback,
      State
    )
  }

  override suspend fun start(): String {
    val clientId = options["google.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["google.scopes"] as List<String>
    val callback = options["callback"] as String
    val state = options["state"] as String

    val scopesString = scopes.joinToString("+", transform = { fullScope(it) })

    return "https://accounts.google.com/o/oauth2/v2/auth?client_id=$clientId&response_type=code&scope=$scopesString&access_type=offline&redirect_uri=$callback&prompt=consent&include_granted_scopes=true&state=$state"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["google.clientId"] as String
    val clientSecret = options["google.clientSecret"] as String
    val callback = options["callback"] as String

    val tokenUrl = "https://www.googleapis.com/oauth2/v4/token"
    val body = FormBody.Builder().add("client_id", clientId)
      .add("redirect_uri", callback)
      .add("client_secret", clientSecret)
      .add("code", code)
      .add("grant_type", "authorization_code")
      .build()
    val request = Request.Builder().url(tokenUrl).method("POST", body).build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(
      responseMap["access_token"] as String,
      responseMap["refresh_token"] as String, clientId, clientSecret
    )
  }
}
