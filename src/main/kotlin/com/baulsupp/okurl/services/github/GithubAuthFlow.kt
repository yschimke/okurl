package com.baulsupp.okurl.services.github

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
import java.net.URLEncoder

class GithubAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow<Oauth2Token>(
  serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("github.clientId", "Github Client Id", null, false),
      Prompt("github.clientSecret", "Github Client Secret", null, true),
      Scopes("github.scopes", "Scopes", known = listOf(
        "user", "repo", "gist", "admin:org")),
      Callback,
      State
    )
  }

  override suspend fun start(): String {
    val clientId = options["github.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["github.scopes"] as List<String>
    val callback = options["callback"] as String
    val state = options["state"] as String

    val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

    return "https://github.com/login/oauth/authorize?client_id=$clientId&scope=$scopesString&redirect_uri=$callback&state=$state"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["github.clientId"] as String
    val clientSecret = options["github.clientSecret"] as String
    val callback = options["callback"] as String

    val body = FormBody.Builder().add("client_id", clientId)
      .add("client_id", clientId)
      .add("code", code)
      .add("client_secret", clientSecret)
      .add("redirect_uri", callback)
      .build()
    val request = Request.Builder().url("https://github.com/login/oauth/access_token")
      .header("Accept", "application/json")
      .post(body)
      .build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(responseMap["access_token"] as String)
  }
}
