package com.baulsupp.okurl.services.gdax

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.gdax.model.Account
import com.baulsupp.okurl.services.gdax.model.Product
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.Buffer
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import java.nio.charset.StandardCharsets.UTF_8

class GdaxAuthInterceptor : AuthInterceptor<GdaxCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<GdaxCredentials>(
    "api.gdax.com", "GDAX API", "gdax",
    "https://docs.gdax.com/", "https://www.gdax.com/settings/api"
  ) {

    override fun parseCredentialsString(s: String): GdaxCredentials {
      val parts = s.split(":".toRegex(), 3)
      return GdaxCredentials(parts[0], parts[1], parts[2])
    }

    override fun formatCredentialsString(credentials: GdaxCredentials) =
      "${credentials.apiKey}:${credentials.apiSecret}:${credentials.passphrase}"
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: GdaxCredentials): Response {
    var request = chain.request()

    val timestamp = (System.currentTimeMillis() / 1000).toString()

    val decodedKey = credentials.apiSecret.decodeBase64()!!

    val sink = Buffer()
    request.body?.writeTo(sink)
    val prehash = "" + timestamp + request.method + request.url.encodedPath + sink.snapshot().string(UTF_8)
    val signature = prehash.encodeUtf8().hmacSha256(decodedKey)

    request = request.newBuilder()
      .addHeader("CB-ACCESS-KEY", credentials.apiKey)
      .addHeader("CB-ACCESS-SIGN", signature.base64())
      .addHeader("CB-ACCESS-TIMESTAMP", timestamp)
      .addHeader("CB-ACCESS-PASSPHRASE", credentials.passphrase)
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): GdaxCredentials {
    val apiKey = Secrets.prompt("GDAX API Key", "gdax.apiKey", "", false)
    val apiSecret = Secrets.prompt("GDAX API Secret", "gdax.apiSecret", "", true)
    val apiPassphrase = Secrets.prompt("GDAX Passphrase", "gdax.passphrase", "", true)

    return GdaxCredentials(apiKey, apiSecret, apiPassphrase)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: GdaxCredentials
  ): ValidatedCredentials {
    val accounts = client.queryList<Account>(
      "https://api.gdax.com/accounts",
      TokenValue(credentials)
    )
    return ValidatedCredentials(accounts.map { it.id }.first())
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

    credentialsStore.get(serviceDefinition, tokenSet)?.let {
      completer.withVariable("account-id") {
        client.queryList<Account>("https://api.gdax.com/accounts", tokenSet).map { it.id }
      }
      completer.withVariable("product-id") {
        client.queryList<Product>("https://api.gdax.com/products", tokenSet).map { it.id }
      }
    }

    return completer
  }
}
