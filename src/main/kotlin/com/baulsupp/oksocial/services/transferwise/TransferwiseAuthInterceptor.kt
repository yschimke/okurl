package com.baulsupp.oksocial.services.transferwise

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets

import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

open class TransferwiseAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition(host(), "Transferwise API", "transferwise",
            "https://api-docs.transferwise.com/docs/versions/v1/overview",
            "https://api-docs.transferwise.com/api-explorer/transferwise-api/versions/v1/")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Transferwise Client Id", "transferwise.clientId", "", false)
    val clientSecret = Secrets.prompt("Transferwise Client Secret", "transferwise.clientSecret", "",
            true)

    return TransferwiseAuthFlow.login(client, outputHandler, host(), clientId, clientSecret)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials(client.queryMapValue<String>("https://api.transferwise.com/v1/me", "name"))

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.isRenewable()
  }

  suspend override fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {

    val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", credentials.refreshToken!!)
            .build()
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = Request.Builder().url("https://" + host() + "/oauth/token")
            .post(body)
            .header("Authorization", basic)
            .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
            responseMap["refresh_token"] as String, credentials.clientId,
            credentials.clientSecret)
  }

  override fun hosts(): Set<String> = setOf(host())

  open fun host() = "api.transferwise.com"
}
