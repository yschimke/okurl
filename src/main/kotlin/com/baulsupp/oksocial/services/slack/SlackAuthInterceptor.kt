package com.baulsupp.oksocial.services.slack

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://api.slack.com/docs/oauth
 */
class SlackAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("slack.com", "Slack API", "slack", "https://api.slack.com/",
    "https://api.slack.com/apps")


  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("token", token).build()

    request = request.newBuilder().url(newUrl).build()

    // TODO check for ok=false?

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Slack Client Id", "slack.clientId", "", false)
    val clientSecret = Secrets.prompt("Slack Client Secret", "slack.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "slack.scopes", listOf(
      "bot",
      "channels:history",
      "channels:read",
      "channels:write",
      "chat:write:bot",
      "dnd:read",
      "dnd:write",
      "emoji:read",
      "files:read",
      "files:write:bot",
      "groups:history",
      "groups:read",
      "groups:write",
      "im:history",
      "im:read",
      "im:write",
      "mpim:history",
      "mpim:read",
      "mpim:write",
      "pins:read",
      "pins:write",
      "reactions:read",
      "reactions:write",
      "reminders:read",
      "reminders:write",
      "reminders:write",
      "search:read",
      "stars:read",
      "stars:write",
      "team:read",
      "usergroups:read",
      "usergroups:write",
      "users.profile:read",
      "users.profile:write",
      "users:read",
      "users:write"))

    return SlackAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://slack.com/api/auth.test",
      TokenValue(credentials), "user"))

  override fun hosts(): Set<String> = setOf("slack.com")
}
