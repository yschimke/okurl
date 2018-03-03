package com.baulsupp.oksocial.services.coinbase

import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.TokenValue
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.coinbase.model.AccountList
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.Arrays

class CoinbaseAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.coinbase.com", "Coinbase API", "coinbase", "https://developers.coinbase.com/api/v2/",
      "https://www.coinbase.com/settings/api")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder().header("Authorization", "Bearer ${credentials.accessToken}").header("CB-VERSION", "2017-12-17").build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Coinbase Client Id", "coinbase.clientId", "", false)
    val clientSecret = Secrets.prompt("Coinbase Client Secret", "coinbase.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "coinbase.scopes", Arrays.asList(
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
    ))

    return CoinbaseAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override fun canRenew(credentials: Oauth2Token) = credentials.isRenewable()

  suspend override fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val body = FormBody.Builder()
      .add("client_id", credentials.clientId!!)
      .add("client_secret", credentials.clientSecret!!)
      .add("refresh_token", credentials.refreshToken!!)
      .add("grant_type", "refresh_token")
      .build()

    val request = Request.Builder().url("https://api.coinbase.com/oauth/token")
      .post(body)
      .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "account_id", {
      credentialsStore.get(serviceDefinition(), tokenSet)?.let {
        client.query<AccountList>(
          "https://api.coinbase.com/v2/accounts",
          tokenSet).data.map { it.id }
      }
    })

    return completer
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.coinbase.com/v2/user", TokenValue(credentials), "data", "name"))

  override fun hosts(): Set<String> = setOf("api.coinbase.com")
}
