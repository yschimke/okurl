package com.baulsupp.okurl.services.paypal

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://developer.paypal.com/docs/authentication
 */
open class PaypalAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition: Oauth2ServiceDefinition
    get() = Oauth2ServiceDefinition(
      host(), "Paypal API", shortName(),
      "https://developer.paypal.com/docs/api/",
      "https://developer.paypal.com/developer/applications/"
    )

  open fun shortName() = "paypal"

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.paypal.com/v1/oauth2/token/userinfo?schema=openid",
        TokenValue(credentials), "name"
      )
    )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Paypal Client Id", "paypal.clientId", "", false)
    val clientSecret = Secrets.prompt("Paypal Client Secret", "paypal.clientSecret", "", true)

    return PaypalAuthFlow.login(client, host(), clientId, clientSecret)
  }

  open fun host() = "api.paypal.com"
}
