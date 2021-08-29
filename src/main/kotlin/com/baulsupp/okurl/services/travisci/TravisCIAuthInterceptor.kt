package com.baulsupp.okurl.services.travisci

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.travisci.model.RepositoryList
import com.baulsupp.okurl.services.travisci.model.User
import com.github.pgreze.process.Redirect.CAPTURE
import com.github.pgreze.process.process
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TravisCIAuthInterceptor : AuthInterceptor<TravisToken>() {
  override val serviceDefinition = object :
    AbstractServiceDefinition<TravisToken>(
      "api.travis-ci.org", "Travis CI API", "travisci",
      "https://docs.travis-ci.com/api/", null
    ) {
    override fun parseCredentialsString(s: String): TravisToken {
      return TravisToken(s)
    }

    override fun formatCredentialsString(credentials: TravisToken): String {
      return credentials.token!!
    }
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: TravisToken): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Travis-API-Version", "3")
      .addHeader("Authorization", "token " + credentials.token)
      .build()

    var response = chain.proceed(request)

    // TODO incorrect response type for errors
    if (!response.isSuccessful && response.header("Content-Type") == "application/json") {
      val newBody = response.body!!.bytes().toResponseBody(null)
      response = response.newBuilder().body(newBody).removeHeader("Content-Type").build()
    }

    return response
  }

  override fun defaultCredentials(): TravisToken? = TravisToken.external()

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): TravisToken {
    if (!isTravisInstalled()) {
      throw UsageException("Requires travis command line installed")
    }

    // TODO support pro as well
    val token = travisToken(false)

    return TravisToken(token)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private suspend fun isTravisInstalled(): Boolean = process("which", "travis").resultCode == 0

  @OptIn(ExperimentalCoroutinesApi::class)
  private suspend fun travisToken(pro: Boolean): String {
    val result = process(
      "travis", "token", "-E", "--no-interactive", if (pro) "--pro" else "--org",
      stdout = CAPTURE
    ).apply {
      if (this.resultCode != 0) throw UsageException("Use 'travis login --org' or 'travis login --pro'")
    }

    return result.output.firstOrNull()?.trim() ?: throw UsageException("no travis token")
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TravisToken
  ): ValidatedCredentials {
    val user = client.query<User>(
      "https://api.travis-ci.org/user",
      TokenValue(credentials)
    )
    return ValidatedCredentials(user.name)
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

    completer.withVariable("user.id") {
      listOf(client.query<User>("https://api.travis-ci.org/user", tokenSet).id)
    }
    completer.withVariable("repository.id") {
      client.query<RepositoryList>(
        "https://api.travis-ci.org/repos",
        tokenSet
      ).repositories.map { it.slug.replace("/", "%2F") }
    }

    return completer
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.travis-ci.com", "api.travis-ci.org")
}
