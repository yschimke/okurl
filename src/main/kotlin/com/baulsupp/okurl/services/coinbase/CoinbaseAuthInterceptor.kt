package com.baulsupp.okurl.services.coinbase

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
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
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.kotlin.requestBuilder
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.coinbase.model.AccountList
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CoinbaseAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.coinbase.com", "Coinbase API", "coinbase", "https://developers.coinbase.com/api/v2/",
    "https://www.coinbase.com/settings/api"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder().header("Authorization", "Bearer ${credentials.accessToken}")
      .header("CB-VERSION", "2017-12-17").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Coinbase Client Id", "coinbase.clientId", "", false)
    val clientSecret = Secrets.prompt("Coinbase Client Secret", "coinbase.clientSecret", "", true)
    val scopes = Secrets.promptArray(
      "Scopes", "coinbase.scopes", listOf(
        "wallet:accounts:read",
        "wallet:addresses:read",
        "wallet:buys:read",
        "wallet:checkouts:read",
        "wallet:deposits:read",
        "wallet:notifications:read",
        "wallet:orders:read",
        "wallet:payment-methods:read",
        "wallet:payment-methods:limits",
        "wallet:sells:read",
        "wallet:transactions:read",
        "wallet:user:read",
        "wallet:withdrawals:read"
//            "wallet:accounts:update",
//            "wallet:accounts:create",
//            "wallet:accounts:delete",
//            "wallet:addresses:create",
//            "wallet:buys:create",
//            "wallet:checkouts:create",
//            "wallet:deposits:create",
//            "wallet:orders:create",
//            "wallet:orders:refund",
//            "wallet:payment-methods:delete",
//            "wallet:sells:create",
//            "wallet:transactions:send",
//            "wallet:transactions:request",
//            "wallet:transactions:transfer",
//            "wallet:user:update",
//            "wallet:user:email",
//            "wallet:withdrawals:create"
      )
    )

    return CoinbaseAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val body = FormBody.Builder()
      .add("client_id", credentials.clientId!!)
      .add("client_secret", credentials.clientSecret!!)
      .add("refresh_token", credentials.refreshToken!!)
      .add("grant_type", "refresh_token")
      .build()

    val request = requestBuilder(
      "https://api.coinbase.com/oauth/token",
      TokenValue(credentials)
    )
      .post(body)
      .build()

    val responseMap = client.queryMap<Any>(request)

    // TODO check if refresh token in response?
    return Oauth2Token(
      responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret
    )
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

    completer.withCachedVariable(name(), "account_id") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<AccountList>(
          "https://api.coinbase.com/v2/accounts",
          tokenSet
        ).data.map { it.id }
      }
    }

    return completer
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.coinbase.com/v2/user",
        TokenValue(credentials), "data", "name"
      )
    )
}
