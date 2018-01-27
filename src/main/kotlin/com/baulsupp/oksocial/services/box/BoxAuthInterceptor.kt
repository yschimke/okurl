package com.baulsupp.oksocial.services.box

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BoxAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.box.com", "Box API", "box",
      "https://developer.box.com/reference", "https://app.box.com/developers/console/")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Box Client Id", "box.clientId", "", false)
    val clientSecret = Secrets.prompt("Box Client Secret", "box.clientSecret", "", true)

    val scopes = Secrets.promptArray("Scopes", "box.scopes", listOf("item_read",
      "item_readwrite",
      "item_preview",
      "item_upload",
      "item_share",
      "item_delete",
      "item_download"))

    return BoxAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.box.com/2.0/users/me", "name"))

  override fun canRenew(credentials: Oauth2Token): Boolean = false

//  suspend override fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
//
//    val body = RequestBody.create(MediaType.parse("application/json"),
//      "{\"grant_type\": \"refresh_token\", \"refresh_token\": \""
//        + credentials.refreshToken + "\"}")
//    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
//    val request = Request.Builder().url("https://api.box.com/oauth/token")
//      .post(body)
//      .header("Authorization", basic)
//      .build()
//
//    val responseMap = AuthUtil.makeJsonMapRequest(client, request)
//
//    // TODO check if refresh token in response?
//    return Oauth2Token(responseMap["access_token"] as String,
//      credentials.refreshToken, credentials.clientId,
//      credentials.clientSecret)
//  }

  override fun hosts(): Set<String> = setOf("api.box.com")
}
