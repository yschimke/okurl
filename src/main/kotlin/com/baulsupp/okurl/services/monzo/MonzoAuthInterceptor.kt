package com.baulsupp.okurl.services.monzo

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.OkHttpClient
import okhttp3.Response

class MonzoAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "api.monzo.com", "Monzo API", "monzo",
      "https://docs.monzo.com/", "https://developers.monzo.com/api/playground"
    )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val apiKey = Secrets.prompt("Monzo Access Token", "monzo.accessToken", "", false)

    return Oauth2Token(apiKey)
  }
}
