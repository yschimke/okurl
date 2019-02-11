package com.baulsupp.okurl.services.atlassian

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.postJsonBody
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ExchangeRequest(
  val grant_type: String = "authorization_code",
  val client_id: String,
  val client_secret: String,
  val code: String,
  val redirect_uri: String
)

data class ExchangeResponse(
  val access_token: String,
  val scope: String?,
  val expires_in: Double,
  val token_type: String,
  val refresh_token: String?
)

data class RefreshRequest(
  val grant_type: String = "refresh_token",
  val client_id: String,
  val client_secret: String,
  val refresh_token: String
)

class AtlassianAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : Oauth2Flow(
  serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("atlassian.clientId", "Atlassian Application Id", null, false),
      Prompt("atlassian.clientSecret", "Atlassian Application Secret", null, true),
      Scopes("atlassian.scopes", "Scopes", known = listOf(
        "read:jira-user", "read:jira-work", "write:jira-work", "offline_access")),
      Callback
    )
  }

  override suspend fun start(): String {
    val clientId = options["atlassian.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["atlassian.scopes"] as List<String>
    val callback = options["callback"] as String
    val state = options["state"] as String

    val serverUriEncoded = URLEncoder.encode(callback, StandardCharsets.UTF_8)
    val scopeString =
      URLEncoder.encode(scopes.joinToString(" "), StandardCharsets.UTF_8).replace("+", "%20")

    return "https://auth.atlassian.com/authorize?" +
      "audience=api.atlassian.com&client_id=$clientId&scope=$scopeString&" +
      "redirect_uri=$serverUriEncoded&response_type=code&prompt=consent&state=$state"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["atlassian.clientId"] as String
    val clientSecret = options["atlassian.clientSecret"] as String
    val callback = options["callback"] as String

    val responseMap =
      client.query<ExchangeResponse>(request("https://auth.atlassian.com/oauth/token", NoToken) {
        postJsonBody(
          ExchangeRequest(
            client_id = clientId,
            client_secret = clientSecret,
            code = code,
            redirect_uri = callback
          )
        )
      })

    return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
  }
}
