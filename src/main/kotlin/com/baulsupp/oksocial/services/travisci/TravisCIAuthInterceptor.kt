package com.baulsupp.oksocial.services.travisci

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
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

class TravisCIAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.travis-ci.org", "Travis CI API", "travisci",
            "https://docs.travis-ci.com/api/", null)
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    request = request.newBuilder()
            .addHeader("Authorization", "token " + credentials.accessToken)
            .build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {
    TODO()
//    val user = Secrets.prompt("Twilio Account SID", "twilio.accountSid", "", false)
//    val password = Secrets.prompt("Twilio Auth Token", "twilio.authToken", "", true)
//
//    return Oauth2Token(token)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    val map = client.queryMap<Any>("https://api.travis-ci.org/users/")
    TODO()
//    val username = (map["accounts"] as List<Map<String, Any>>)[0]["friendly_name"] as String
//    return ValidatedCredentials(username)
  }

//  override fun apiCompleter(prefix: String, client: OkHttpClient,
//                            credentialsStore: CredentialsStore,
//                            completionVariableCache: CompletionVariableCache): ApiCompleter {
//    val urlList = UrlList.fromResource(name())
//
//    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)
//
//    completer.withVariable("AccountSid", {
//      credentialsStore[serviceDefinition()]?.let { listOf(it.user) }
//    })
//
//    return completer
//  }

  override fun hosts(): Set<String> = setOf("api.travis-ci.org")
}
