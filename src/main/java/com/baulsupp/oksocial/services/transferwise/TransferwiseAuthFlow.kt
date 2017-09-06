package com.baulsupp.oksocial.services.transferwise

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object TransferwiseAuthFlow {
    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, host: String,
              clientId: String, clientSecret: String): Oauth2Token {
        SimpleWebServer.forCode().use { s ->
            val serverUri = s.redirectUri

            val loginUrl = "https://$host/oauth/authorize"
            +"?client_id=" + clientId
            +"&response_type=code"
            +"&scope=transfers"
            +"&redirect_uri=" + serverUri

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val body = FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
                    .add("grant_type", "authorization_code").add("code", code).build()
            val basic = Credentials.basic(clientId, clientSecret)
            val request = Request.Builder().url("https://$host/oauth/token")
                    .post(body)
                    .header("Authorization", basic)
                    .build()

            val responseMap = AuthUtil.makeJsonMapRequest(client, request)

            return Oauth2Token(responseMap["access_token"] as String,
                    responseMap["refresh_token"] as String, clientId, clientSecret)
        }
    }
}
