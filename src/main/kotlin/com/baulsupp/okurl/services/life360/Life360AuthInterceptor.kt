package com.baulsupp.okurl.services.life360

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.life360.model.Circles
import com.baulsupp.okurl.services.life360.model.Token
import com.baulsupp.okurl.services.squareup.model.LocationList
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.asResponseBody

class Life360AuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "www.life360.com", "Life360 API", "life360",
      "https://github.com/kaylathedev/life360-node-api", null
    )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response =
    chain.proceed(chain.request().edit {
      addHeader("Authorization", "Bearer ${credentials.accessToken}")
      if (chain.request().header("Accept") == null) {
        addHeader("Accept", "application/json")
      }
    }).let {
      val body = it.body
      if (body != null && body.contentType()?.subtype == "html" && it.peekBody(1L).string() == "{") {
        it.newBuilder().body(body.source().asResponseBody("application/json".toMediaType(), body.contentLength())).build()
      } else {
        it
      }
    }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val email = Secrets.prompt("Life360 email", "life360.email", "", false)
    val password = Secrets.prompt("Life360 password", "life360.password", "", true)

    val token = client.query<Token>(request("https://www.life360.com/v3/oauth2/token") {
      // dev account from https://github.com/kaylathedev/life360-node-api
      header("Authorization", "Basic U3dlcUFOQWdFVkVoVWt1cGVjcmVrYXN0ZXFhVGVXckFTV2E1dXN3MzpXMnZBV3JlY2hhUHJlZGFoVVJhZ1VYYWZyQW5hbWVqdQ==")
      header("accept", "application/json")

      post(FormBody.Builder()
        .add("username", email)
        .add("password", password)
        .add("grant_type", "password")
        .build())
    })

    return Oauth2Token(token.access_token)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: com.baulsupp.okurl.credentials.Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "circleId", keepTemplate = false) {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<Circles>(
          "https://www.life360.com/v3/circles",
          tokenSet
        ).circles.map { it.id }
      }
    }

    return completer
  }
}
