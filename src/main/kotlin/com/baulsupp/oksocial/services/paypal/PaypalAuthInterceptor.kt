package com.baulsupp.oksocial.services.paypal

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.google.common.collect.Sets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

/**
 * https://developer.paypal.com/docs/authentication
 */
open class PaypalAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition(host(), "Paypal API", shortName(),
        "https://developer.paypal.com/docs/api/",
        "https://developer.paypal.com/developer/applications/")
  }

  protected open fun shortName(): String {
    return "paypal"
  }

  protected open fun host(): String {
    return "api.paypal.com"
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val builder = request.newBuilder().addHeader("Authorization", "Bearer " + token)

    request = builder.build()

    return chain.proceed(request)
  }

  override suspend fun validate(client: OkHttpClient,
                        requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
    return JsonCredentialsValidator(
        PaypalUtil.apiRequest("/v1/oauth2/token/userinfo?schema=openid", requestBuilder),
        { it -> it["name"].toString() }).validate(client)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Paypal API")

    val clientId = Secrets.prompt("Paypal Client Id", "paypal.clientId", "", false)
    val clientSecret = Secrets.prompt("Paypal Client Secret", "paypal.clientSecret", "", true)

    return PaypalAuthFlow.login(client, host(), clientId, clientSecret)
  }

  override fun hosts(): Set<String> {
    return Sets.newHashSet(host())
  }
}
