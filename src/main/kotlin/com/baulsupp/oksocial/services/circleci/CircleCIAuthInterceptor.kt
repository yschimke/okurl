package com.baulsupp.oksocial.services.circleci

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
import com.baulsupp.oksocial.kotlin.queryList
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.lyft.com/docs/authentication
 */
class CircleCIAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("circleci.com", "CircleCI API", "circleci",
      "https://circleci.com/docs/api/v1-reference/", "https://circleci.com/account/api")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val newUrl = request.url().newBuilder().addQueryParameter("circle-token", token).build()

    val builder = request.newBuilder()
    if (request.url().encodedPath().startsWith("/api/v1.1/")) {
      builder.addHeader("Accept", "application/json")
    }
    request = builder.url(newUrl).build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token =
    Oauth2Token(Secrets.prompt("CircleCI Personal API Token", "circleci.token", "", true))

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.query<User>("https://circleci.com/api/v1.1/me").name)

  override fun hosts(): Set<String> = setOf("circleci.com")

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "project-path", {
      client.queryList<Project>("https://circleci.com/api/v1.1/projects").map { it.vcs_type + "/" + it.username + "/" + it.reponame }
    })

    return completer
  }
}
