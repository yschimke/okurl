package com.baulsupp.okurl.services.travisci

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
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.output.process.exec
import com.baulsupp.oksocial.output.stdErrLogging
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.travisci.model.RepositoryList
import com.baulsupp.okurl.services.travisci.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody

class TravisCIAuthInterceptor : AuthInterceptor<TravisToken>() {
  override val serviceDefinition = object :
    AbstractServiceDefinition<TravisToken>("api.travis-ci.org", "Travis CI API", "travisci",
      "https://docs.travis-ci.com/api/", null) {
    override fun parseCredentialsString(s: String): TravisToken {
      return TravisToken(s)
    }

    override fun formatCredentialsString(credentials: TravisToken): String {
      return credentials.token!!
    }
  }

  override fun intercept(chain: Interceptor.Chain, credentials: TravisToken): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Travis-API-Version", "3")
      .addHeader("Authorization", "token " + credentials.token)
      .build()

    var response = chain.proceed(request)

    // TODO incorrect response type for errors
    if (!response.isSuccessful && response.header("Content-Type") == "application/json") {
      val newBody = ResponseBody.create(null, response.body()!!.bytes())
      response = response.newBuilder().body(newBody).removeHeader("Content-Type").build()
    }

    return response
  }

  override fun defaultCredentials(): TravisToken? {
    return TravisToken.external()
  }

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

  private suspend fun isTravisInstalled(): Boolean = exec("which", "travis").success

  private suspend fun travisToken(pro: Boolean): String {
    val result = exec(listOf("travis", "token", "-E", "--no-interactive", if (pro) "--pro" else "--org")) {
      readOutput(true)
      redirectError(stdErrLogging)
    }

    if (!result.success) {
      throw UsageException("Use 'travis login --org' or 'travis login --pro'")
    }

    return result.outputString.trim()
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TravisToken
  ): ValidatedCredentials {
    val user = client.query<User>("https://api.travis-ci.org/user",
      TokenValue(credentials))
    return ValidatedCredentials(user.name)
  }

  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withVariable("user.id", {
      listOf(client.query<User>("https://api.travis-ci.org/user", tokenSet).id)
    })
    completer.withVariable("repository.id", {
      client.query<RepositoryList>("https://api.travis-ci.org/repos",
        tokenSet).repositories.map { it.slug.replace("/", "%2F") }
    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.travis-ci.com", "api.travis-ci.org")
}
