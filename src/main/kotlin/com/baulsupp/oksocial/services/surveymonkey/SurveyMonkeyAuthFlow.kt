package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object SurveyMonkeyAuthFlow {
    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
              apiKey: String, secret: String): Oauth2Token {
        SimpleWebServer.forCode().use { s ->
            val redirectUri = s.redirectUri

            val loginUrl = "https://api.surveymonkey.net/oauth/authorize?response_type=code&client_id=$clientId&api_key=$apiKey&redirect_uri=$redirectUri"

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val body = FormBody.Builder().add("client_secret", secret)
                    .add("code", code)
                    .add("redirect_uri", redirectUri)
                    .add("grant_type", "authorization_code")
                    .build()

            val request = Request.Builder().url("https://api.surveymonkey.net/oauth/token")
                    .post(body)
                    .build()

            val responseMap = AuthUtil.makeJsonMapRequest(client, request)

            return Oauth2Token(responseMap["access_token"] as String)
        }
    }
}
