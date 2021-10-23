package com.baulsupp.okurl.services.snapkit

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.authflow.State
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.services.strava.model.AuthResponse

class SnapkitAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow<Oauth2Token>(
  serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("snapkit.clientId", "Snapkit Client Id", null, false),
      Prompt("snapkit.clientSecret", "Snapkit Client Secret", null, false),
      Scopes("snapkit.scopes", "Snapkit Scopes", known = listOf(
        "https://auth.snapchat.com/oauth2/api/user.display_name",
        "https://auth.snapchat.com/oauth2/api/user.bitmoji.avatar",
        "https://auth.snapchat.com/oauth2/api/user.external_id")),
      Callback,
      State
    )
  }

  override suspend fun start(): String {
    val clientId = options["snapkit.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["snapkit.scopes"] as List<String>
    val callback = options["callback"] as String
    val state = options["state"] as String

    return "https://accounts.snapchat.com/accounts/oauth2/auth?client_id=$clientId&redirect_uri=$callback&response_type=code&scope=${scopes.joinToString(
      "+")}&state=$state"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["snapkit.clientId"] as String

    val responseMap =
      client.query<AuthResponse>(request("https://bitmoji.api.snapchat.com/direct/token") {
        post(
          form {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("code", code)
          }
        )
      })

    return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId,
      null)
  }
}
