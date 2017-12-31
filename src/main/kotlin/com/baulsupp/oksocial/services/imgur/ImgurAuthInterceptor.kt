package com.baulsupp.oksocial.services.imgur

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class ImgurAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.imgur.com", "Imgur API", "imgur",
            "https://api.imgur.com/endpoints", "https://imgur.com/account/settings/apps")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false)
    val clientSecret = Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true)

    return ImgurAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
          ValidatedCredentials(client.queryMapValue<String>("https://api.imgur.com/3/account/me", "data", "url"))

  override fun canRenew(result: Response): Boolean = result.code() == 403

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  suspend override fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val body = FormBody.Builder().add("refresh_token", credentials.refreshToken!!)
            .add("client_id", credentials.clientId!!)
            .add("client_secret", credentials.clientSecret!!)
            .add("grant_type", "refresh_token")
            .build()
    val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
            .method("POST", body)
            .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    // TODO check if refresh token in response?
    return Oauth2Token(responseMap["access_token"] as String,
            credentials.refreshToken, credentials.clientId,
            credentials.clientSecret)
  }

  override fun hosts(): Set<String> = setOf("api.imgur.com")
}
