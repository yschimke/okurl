package com.baulsupp.oksocial.services.paypal

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.paypal.com/docs/authentication
 */
open class PaypalAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition(host(), "Paypal API", shortName(),
            "https://developer.paypal.com/docs/api/",
            "https://developer.paypal.com/developer/applications/")
  }

  open fun shortName() = "paypal"

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val builder = request.newBuilder().addHeader("Authorization", "Bearer " + token)

    request = builder.build()

    return chain.proceed(request)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials(client.queryMapValue<String>("https://api.paypal.com/v1/oauth2/token/userinfo?schema=openid", "name"))

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Paypal Client Id", "paypal.clientId", "", false)
    val clientSecret = Secrets.prompt("Paypal Client Secret", "paypal.clientSecret", "", true)

    return PaypalAuthFlow.login(client, host(), clientId, clientSecret)
  }

  override fun hosts(): Set<String> = setOf(host())

  open fun host() = "api.paypal.com"
}
