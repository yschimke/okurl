package com.baulsupp.oksocial.services.lyft

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.google.common.collect.Lists
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

/**
 * https://developer.lyft.com/docs/authentication
 */
class LyftAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.lyft.com", "Lyft API", "lyft",
        "https://developer.lyft.com/docs", "https://www.lyft.com/developers/manage")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Lyft API")

    val clientId = Secrets.prompt("Lyft Client Id", "lyft.clientId", "", false)
    val clientSecret = Secrets.prompt("Lyft Client Secret", "lyft.clientSecret", "", true)

    return if (authArguments == Lists.newArrayList("--client")) {
      LyftClientAuthFlow.login(client, clientId, clientSecret)
    } else {
      val scopes = Secrets.promptArray("Scopes", "lyft.scopes", LyftUtil.SCOPES)

      LyftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }
  }

  override suspend fun validate(client: OkHttpClient,
                                requestBuilder: Request.Builder, credentials: Oauth2Token): ValidatedCredentials {
    return JsonCredentialsValidator(
        LyftUtil.apiRequest("/v1/profile", requestBuilder), { it["id"] as String }).validate(
        client)
  }

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.isRenewable()
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {

    val body = RequestBody.create(MediaType.parse("application/json"),
        "{\"grant_type\": \"refresh_token\", \"refresh_token\": \""
            + credentials.refreshToken + "\"}")
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = Request.Builder().url("https://api.lyft.com/oauth/token")
        .post(body)
        .header("Authorization", basic)
        .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
        credentials.refreshToken, credentials.clientId,
        credentials.clientSecret)
  }

  override fun hosts(): Set<String> {
    return LyftUtil.API_HOSTS
  }
}
