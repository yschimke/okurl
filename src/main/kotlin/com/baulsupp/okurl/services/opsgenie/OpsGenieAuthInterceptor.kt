package com.baulsupp.okurl.services.opsgenie

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.authflow.PromptDesktopFlow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.services.opsgenie.model.AccountResponse
import com.baulsupp.okurl.services.opsgenie.model.AlertsResponse
import com.baulsupp.okurl.services.opsgenie.model.SchedulesResponse
import com.baulsupp.okurl.services.opsgenie.model.TeamsResponse
import com.baulsupp.okurl.services.opsgenie.model.UsersResponse
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
  ): Oauth2Token = PromptDesktopFlow.prompt(OpsGenieAuthFlow(serviceDefinition), client)

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val teams = client.query<AccountResponse>("https://api.opsgenie.com/v2/account", TokenValue(credentials))
    return ValidatedCredentials(teams.data.name)
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.opsgenie.com")

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "teamId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<TeamsResponse>("https://api.opsgenie.com/v2/teams", tokenSet).data.map { it.id }
      }
    }

    completer.withCachedVariable(name(), "userId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<UsersResponse>("https://api.opsgenie.com/v2/users", tokenSet).data.map { it.username }
      }
    }

    completer.withCachedVariable(name(), "scheduleId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<SchedulesResponse>("https://api.opsgenie.com/v2/schedules", tokenSet).data.map { it.id }
      }
    }

    completer.withCachedVariable(name(), "alertId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<AlertsResponse>("https://api.opsgenie.com/v2/alerts", tokenSet).data.map { it.id }
      }
    }

    return completer
  }
}
