package com.baulsupp.oksocial.services.twilio

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TwilioAuthInterceptor : AuthInterceptor<BasicCredentials> {
  override fun serviceDefinition(): BasicAuthServiceDefinition {
    return BasicAuthServiceDefinition("api.twilio.com", "Twilio API", "twilio",
            "https://www.twilio.com/docs/api/rest", "https://www.twilio.com/console")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
            .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): BasicCredentials {
    val user = Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false)
    val password = Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true)

    return BasicCredentials(user, password)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: BasicCredentials): ValidatedCredentials {
    val map = client.queryMap<Any>("https://api.twilio.com/2010-04-01/Accounts.json")
    val username = (map["accounts"] as List<Map<String, Any>>)[0]["friendly_name"] as String
    return ValidatedCredentials(username)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val credentials = credentialsStore.readDefaultCredentials(serviceDefinition())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    if (credentials != null) {
      completer.withVariable("AccountSid", { listOf(credentials.user) })
    }

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.twilio.com")
}
