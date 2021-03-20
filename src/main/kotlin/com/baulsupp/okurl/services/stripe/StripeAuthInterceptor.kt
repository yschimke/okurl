package com.baulsupp.okurl.services.stripe

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.OkHttpClient
import okhttp3.Response

class StripeAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "api.stripe.com", "Stripe API", "stripe",
      "https://stripe.com/docs/api", "https://dashboard.stripe.com/test/apikeys"
    )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val apiKey = Secrets.prompt("Stripe Access Token", "stripe.accessToken", "", false)

    return Oauth2Token(apiKey)
  }
}
