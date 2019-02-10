package com.baulsupp.okurl.services.github

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.FormBody
import okhttp3.Request
import java.net.URLEncoder

class GithubAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow(
  serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("github.clientId", "Github Client Id", null, false),
      Prompt("github.clientSecret", "Github Client Secret", null, true),
      Scopes("github.scopes", "Scopes", known = listOf(
        "user", "repo", "gist", "admin:org")),
      Callback
    )
  }

  val startOptions = mutableMapOf<String, Any>()

  override suspend fun start(options: Map<String, Any>): String {
    this.startOptions.putAll(options)

    val clientId = startOptions["github.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = startOptions["github.scopes"] as List<String>
    val callback = startOptions["callback"] as String

    val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

    return "https://github.com/login/oauth/authorize?client_id=$clientId&scope=$scopesString&redirect_uri=$callback"

  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = startOptions["github.clientId"] as String
    val clientSecret = startOptions["github.clientSecret"] as String
    val callback = startOptions["callback"] as String

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
