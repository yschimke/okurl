package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.twitter.joauth.Signature
import com.baulsupp.okurl.services.twitter.twurlrc.TwurlrcImport
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TwitterAuthInterceptor : AuthInterceptor<TwitterCredentials>() {

  override val serviceDefinition = TwitterServiceDefinition()

  override suspend fun intercept(chain: Interceptor.Chain, credentials: TwitterCredentials): Response {
    var request = chain.request()

    val authHeader = Signature().generateAuthorization(request, credentials)
    request = request.newBuilder().addHeader("Authorization", authHeader).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): TwitterCredentials {

    if (!authArguments.isEmpty() && authArguments[0] == "--twurlrc") {
      return TwurlrcImport.authorize(authArguments)
    }

    if (authArguments == listOf("--pin")) {
      val consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false)
      val consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true)

      return PinAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret)
    }

    if (!authArguments.isEmpty()) {
      throw UsageException(
        "unexpected arguments to --authorize twitter: " + authArguments.joinToString(" "))
    }

    val consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false)
    val consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true)

    return WebAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TwitterCredentials
  ): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.twitter.com/1.1/account/verify_credentials.json",
      TokenValue(credentials), "name"))

  override fun hosts(): Set<String> = TwitterUtil.TWITTER_API_HOSTS
}
