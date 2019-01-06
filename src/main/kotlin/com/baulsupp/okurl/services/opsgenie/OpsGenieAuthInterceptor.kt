package com.baulsupp.okurl.services.opsgenie

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class OpsGenieAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.opsgenie.com", "OpsGenie API", "opsgenie",
    "https://docs.opsgenie.com/docs/api-overview", "https://app.opsgenie.com/integration"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder().addHeader("Authorization", "GenieKey ${credentials.accessToken}").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val apiKey = Secrets.prompt("OpsGenie API Key", "opsgenie.apiKey", "", false)

    return Oauth2Token(apiKey)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val teams = client.query<TeamsResponse>("https://api.opsgenie.com/v2/teams", TokenValue(credentials))
    return ValidatedCredentials(teams.data.firstOrNull()?.name)
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.opsgenie.com")
}
