package com.baulsupp.okurl.services.surveymonkey

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
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
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.surveymonkey.model.SurveyList
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.surveymonkey.com/docs/authentication
 */
class SurveyMonkeyAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("api.surveymonkey.net", "Survey Monkey API", "surveymonkey",
    "https://developer.surveymonkey.com/api/v3/#scopes",
    "https://developer.surveymonkey.com/apps/")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val newRequest = chain.request().newBuilder().addHeader("Authorization",
      "bearer " + credentials.accessToken).build()

    return chain.proceed(newRequest)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("SurveyMonkey Client ID", "surveymonkey.clientId", "", false)
    val clientSecret = Secrets.prompt("SurveyMonkey Client Secret", "surveymonkey.clientSecret", "",
      true)
    return SurveyMonkeyAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  data class User(
    val username: String,
    val first_name: String?,
    val last_name: String?,
    val email: String
  )

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val user = client.query<User>("https://api.surveymonkey.net/v3/users/me",
      TokenValue(credentials))

    return if (user.first_name != null && user.last_name != null) {
      ValidatedCredentials(user.first_name + " " + user.last_name)
    } else {
      ValidatedCredentials(user.email)
    }
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

    completer.withCachedVariable(name(), "survey") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<SurveyList>("https://api.surveymonkey.net/v3/surveys", tokenSet).data.map { m -> m.id }
      }
    }

    return completer
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.surveymonkey.net")
}
