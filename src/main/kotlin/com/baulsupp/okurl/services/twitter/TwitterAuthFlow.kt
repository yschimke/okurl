package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.authenticator.authflow.AuthOption
import com.baulsupp.okurl.authenticator.authflow.Callback
import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.Scopes
import com.baulsupp.okurl.authenticator.authflow.State
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.squareup.moshi.JsonClass
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@JsonClass(generateAdapter = true)
data class ExchangeResponse(
  val access_token: String,
  val refresh_token: String?,
  val scope: String?,
  val expires_in: Double,
  val token_type: String,
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
  val grant_type: String = "refresh_token",
  val client_id: String,
  val client_secret: String,
  val refresh_token: String
)

class TwitterAuthFlow(
  override val serviceDefinition: ServiceDefinition<Oauth2Token>
) : Oauth2Flow<Oauth2Token>(serviceDefinition) {
  override fun options(): List<AuthOption<*>> {
    return listOf(
      Prompt("twitter.clientId", "Twitter Client Id", null, false),
      Prompt("twitter.clientSecret", "Twitter Client Secret", null, true),
      Scopes(
        "twitter.scopes", "Scopes", known = listOf(
        "offline.access", "tweet.write", "users.read", "like.write", "tweet.moderate.write",
        "follows.write", "block.write", "mute.write", "space.read", "list.write",

      )
      ),
      Callback,
      State
    )
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun start(): String {
    val clientId = options["twitter.clientId"] as String
    @Suppress("UNCHECKED_CAST") val scopes = options["twitter.scopes"] as List<String>
    val callback = options["callback"] as String
    val state = options["state"] as String

    val serverUriEncoded = URLEncoder.encode(callback, StandardCharsets.UTF_8.name())
    val scopeString =
      URLEncoder.encode(scopes.joinToString(" "), StandardCharsets.UTF_8.name()).replace("+", "%20")

    return "https://twitter.com/i/oauth2/authorize" +
      "?response_type=code&client_id=$clientId&redirect_uri=$serverUriEncoded" +
      "&scope=${scopeString}" +
      "&state=$state&code_challenge=challenge&code_challenge_method=plain"
  }

  override suspend fun complete(code: String): Oauth2Token {
    val clientId = options["twitter.clientId"] as String
    val clientSecret = options["twitter.clientSecret"] as String
    val callback = options["callback"] as String

    val request = request("https://api.twitter.com/2/oauth2/token", NoToken) {
      post(form {
        add("grant_type", "authorization_code")
        add("client_id", clientId)
        add("code", code)
        add("redirect_uri", callback)
        add("code_verifier", "challenge")
      })
    }

    val responseMap =
      client.query<ExchangeResponse>(request)

    return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
  }
}
