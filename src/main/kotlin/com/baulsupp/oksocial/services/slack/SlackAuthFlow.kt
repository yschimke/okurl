package com.baulsupp.oksocial.services.slack

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.net.URLEncoder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

import java.util.stream.Collectors.joining

object SlackAuthFlow {
    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
              clientSecret: String, scopes: Set<String>): Oauth2Token {
        SimpleWebServer.forCode().use { s ->

            val scopesString = URLEncoder.encode(scopes.stream().collect<String, *>(joining(" ")), "UTF-8")

            val loginUrl = "https://slack.com/oauth/authorize"
            +"?client_id=" + clientId
            +"&redirect_uri=" + s.redirectUri
            +"&scope=" + scopesString

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val url = HttpUrl.parse("https://api.slack.com/api/oauth.access")!!
                    .newBuilder()
                    .addQueryParameter("client_id", clientId)
                    .addQueryParameter("client_secret", clientSecret)
                    .addQueryParameter("redirect_uri", s.redirectUri)
                    .addQueryParameter("code", code)
                    .build()

            val request = Request.Builder().url(url).build()

            val responseMap = AuthUtil.makeJsonMapRequest(client, request)

            if (responseMap["ok"] != true) {
                throw IOException("authorization failed: " + responseMap["error"])
            }

            return Oauth2Token(responseMap["access_token"] as String)
        }
    }
}
