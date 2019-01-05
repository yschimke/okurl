package com.baulsupp.okurl.services.atlassian

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.postJsonBody
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.atlassian.model.AccessibleResource
import com.baulsupp.okurl.services.atlassian.model.Myself
import okhttp3.OkHttpClient
import okhttp3.Response

class AtlassianAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.atlassian.com", "Atlassian API", "atlassian",
    "https://developer.atlassian.com/cloud/jira/platform/rest/", "https://developer.atlassian.com/apps/"
  )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val clientId = Secrets.prompt("Atlassian Application Id", "atlassian.clientId", "", false)
    val clientSecret = Secrets.prompt("Atlassian Application Secret", "atlassian.clientSecret", "", true)
    val scopes = Secrets.promptArray(
      "Atlassian Scopes",
      "atlassian.scopes",
      listOf("read:jira-user", "read:jira-work", "write:jira-work", "offline_access")
    )

    return AtlassianAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "cloudid") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.queryList<AccessibleResource>(
          "https://api.atlassian.com/oauth/token/accessible-resources",
          tokenSet
        ).map { it.id }
      }
    }

    return completer
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val resources = client.queryList<AccessibleResource>("https://api.atlassian.com/oauth/token/accessible-resources")

    val instanceId = resources.firstOrNull()?.id ?: return ValidatedCredentials()

    val user = client.query<Myself>("https://api.atlassian.com/ex/jira/$instanceId/rest/api/3/myself")

    return ValidatedCredentials(user.displayName)
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.atlassian.com")

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val responseMap = client.query<ExchangeResponse>(request("https://auth.atlassian.com/oauth/token", NoToken) {
      postJsonBody(
        RefreshRequest(
          client_id = credentials.clientId!!,
          client_secret = credentials.clientSecret!!,
          refresh_token = credentials.refreshToken!!
        )
      )
    })

    return Oauth2Token(
      responseMap.access_token,
      credentials.refreshToken,
      credentials.clientId,
      credentials.clientSecret
    )
  }
}
