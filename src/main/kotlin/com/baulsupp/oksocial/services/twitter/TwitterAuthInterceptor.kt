package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.twitter.twurlrc.TwurlrcImport
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TwitterAuthInterceptor : AuthInterceptor<TwitterCredentials> {

  override fun serviceDefinition(): TwitterServiceDefinition {
    return TwitterServiceDefinition()
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: TwitterCredentials): Response {
    var request = chain.request()

    val authHeader = Signature().generateAuthorization(request, credentials)
    request = request.newBuilder().addHeader("Authorization", authHeader).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): TwitterCredentials {
    System.err.println("Authorising Twitter API")

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

  override suspend fun validate(client: OkHttpClient,
                                credentials: TwitterCredentials): ValidatedCredentials {
    return JsonCredentialsValidator(
            TwitterUtil.apiRequest("/1.1/account/verify_credentials.json", Request.Builder()),
            { it["name"] as String }).validate(client)
  }

  override fun hosts(): Set<String> {
    return TwitterUtil.TWITTER_API_HOSTS
  }
}
