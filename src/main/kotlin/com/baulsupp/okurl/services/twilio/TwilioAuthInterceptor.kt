package com.baulsupp.okurl.services.twilio

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.twilio.model.Accounts
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TwilioAuthInterceptor : AuthInterceptor<BasicCredentials>() {
  override val serviceDefinition: BasicAuthServiceDefinition =
    BasicAuthServiceDefinition(
      "api.twilio.com", "Twilio API", "twilio",
      "https://www.twilio.com/docs/api/rest", "https://www.twilio.com/console"
    )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): BasicCredentials {
    val user = Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false)
    val password = Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true)

    return BasicCredentials(user, password)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: BasicCredentials
  ): ValidatedCredentials {
    val map = client.query<Accounts>(
      "https://api.twilio.com/2010-04-01/Accounts.json",
      TokenValue(credentials)
    )
    return ValidatedCredentials(map.accounts.firstOrNull()?.friendly_name)
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

    completer.withVariable("AccountSid") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let { listOf(it.user) }
    }

    return completer
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.twilio.com")
}
