package com.baulsupp.oksocial.services.paypal

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object PaypalAuthFlow {
  suspend fun login(client: OkHttpClient, host: String, clientId: String,
                    clientSecret: String): Oauth2Token {
    val body = FormBody.Builder().add("grant_type", "client_credentials").build()

    val basic = Credentials.basic(clientId, clientSecret)
    val request = Request.Builder().url("https://$host/v1/oauth2/token")
      .post(body)
      .header("Authorization", basic)
      .header("Accept", "application/json")
      .build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    return Oauth2Token(responseMap["access_token"] as String)
  }
}
