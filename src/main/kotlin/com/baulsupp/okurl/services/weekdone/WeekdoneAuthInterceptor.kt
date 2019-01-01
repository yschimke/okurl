package com.baulsupp.okurl.services.weekdone

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

data class RefreshResponse(val access_token: String)

class WeekdoneAuthInterceptor : AuthInterceptor<Oauth2Token>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val signedUrl = request.url().newBuilder().addQueryParameter("token", credentials.accessToken).build()

    request = request.newBuilder().url(signedUrl).build()

    return chain.proceed(request)
  }

  override val serviceDefinition =
    Oauth2ServiceDefinition(
      "api.weekdone.com", "Weekdone", "weekdone",
      "https://weekdone.com/developer/", "https://weekdone.com/settings?tab=applications"
    )

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
//      val body = credentials.accessToken.decodeBase64()
//    println(body!!.utf8())

    return ValidatedCredentials(null, null)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    return WeekdoneAuthFlow.login(client, outputHandler)
  }

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val response = client.query<RefreshResponse>(request("https://weekdone.com/oauth_token") {
      post(
        FormBody.Builder().add("refresh_token", credentials.refreshToken!!).add(
          "grant_type",
          "refresh_token"
        ).add("redirect_uri", "http://localhost:3000/callback").add(
          "client_id",
          credentials.clientId!!
        ).add("client_secret", credentials.clientSecret!!).build()
      )
    })

    return Oauth2Token(
      response.access_token,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret
    )
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.weekdone.com")
}
