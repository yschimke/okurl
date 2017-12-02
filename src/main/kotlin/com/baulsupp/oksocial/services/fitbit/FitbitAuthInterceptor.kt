package com.baulsupp.oksocial.services.fitbit

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Arrays

class FitbitAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.fitbit.com", "Fitbit API", "fitbit",
            "https://dev.fitbit.com/docs/", "https://dev.fitbit.com/apps/")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
          authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Fitbit API")

    val clientId = Secrets.prompt("Fitbit Client Id", "fitbit.clientId", "", false)
    val clientSecret = Secrets.prompt("Fitbit Client Secret", "fitbit.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "fitbit.scopes",
            Arrays.asList("activity", "heartrate", "location", "nutrition", "profile",
                    "settings", "sleep", "social", "weight"))

    return FitbitAuthCodeFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    return JsonCredentialsValidator(
            Request.Builder().url("https://api.fitbit.com/1/user/-/profile.json").build(),
            { this.getName(it) }).validate(client)
  }

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.refreshToken != null
            && credentials.clientId != null
            && credentials.clientSecret != null
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val body = FormBody.Builder().add("grant_type", "refresh_token")
            .add("refresh_token", credentials.refreshToken!!)
            .build()
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = Request.Builder().url("https://api.fitbit.com/oauth2/token")
            .post(body)
            .header("Authorization", basic)
            .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
            credentials.refreshToken!!, credentials.clientId!!,
            credentials.clientSecret!!)
  }

  private fun getName(map: Map<String, Any>): String {
    val user = map["user"] as Map<String, Any>

    return user["fullName"] as String
  }

  override fun hosts(): Set<String> {
    return setOf((
            "api.fitbit.com")
    )
  }
}
