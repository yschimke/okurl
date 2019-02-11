package com.baulsupp.okurl.services.strava

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request

data class AuthResponse(
  val token_type: String,
  val access_token: String,
  val athlete: Map<String, Any>?,
  val refresh_token: String,
  val expires_at: Long,
  val state: String? = null
)

class StravaAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow(
  serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("strava.clientId", "Strava Client Id", null, false),
      Prompt("strava.clientSecret", "Strava Client Secret", null, true),
      Scopes("strava.scopes", "Strava Scopes", known = listOf(
        "read_all",
        "profile:read_all",
        "profile:write",
        "activity:read_all",
        "activity:write")),
      Callback
    )
  }

  override suspend fun start(): String {
    val clientId = options["strava.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["strava.scopes"] as List<String>
    val callback = options["callback"] as String

    return "https://www.strava.com/oauth/authorize?client_id=$clientId&redirect_uri=${callback}&response_type=code&scope=${scopes.joinToString(
      ",")}"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["strava.clientId"] as String
    val clientSecret = options["strava.clientSecret"] as String

    val responseMap =
      client.query<AuthResponse>(request("https://www.strava.com/oauth/token") {
        post(
          form {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code", code)
          }
        )
      })

    return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId,
      clientSecret)
  }
}
