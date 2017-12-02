package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.surveymonkey.model.SurveyList
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

/**
 * https://developer.surveymonkey.com/docs/authentication
 */
class SurveyMonkeyAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.surveymonkey.net", "Survey Monkey API", "surveymonkey",
            "https://developer.surveymonkey.com/api/v3/#scopes",
            "https://developer.surveymonkey.com/apps/")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val newRequest = chain.request().newBuilder().addHeader("Authorization",
            "bearer " + credentials.accessToken).build()

    return chain.proceed(newRequest)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
          authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising SurveyMonkey API")

    val clientId = Secrets.prompt("SurveyMonkey Client ID", "surveymonkey.clientId", "", false)
    val clientSecret = Secrets.prompt("SurveyMonkey Client Secret", "surveymonkey.clientSecret", "",
            true)
    return SurveyMonkeyAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  data class User(val username: String, val first_name: String?, val last_name: String?,
          val email: String)

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    val user = client.query<User>("https://api.surveymonkey.net/v3/users/me")

    return if (user.first_name != null && user.last_name != null) {
      ValidatedCredentials(user.first_name + " " + user.last_name)
    } else {
      ValidatedCredentials(user.email)
    }
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
          credentialsStore: CredentialsStore,
          completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val credentials = credentialsStore.readDefaultCredentials(serviceDefinition())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    if (credentials != null) {
      completer.withCachedVariable(name(), "survey", {
        client.query<SurveyList>("https://api.surveymonkey.net/v3/surveys").data.map { m -> m.id }
      })
    }

    return completer
  }

  override fun hosts(): Set<String> {
    return setOf("api.surveymonkey.net")
  }
}
