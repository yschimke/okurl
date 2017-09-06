package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.net.URLEncoder
import okhttp3.OkHttpClient

import com.baulsupp.oksocial.authenticator.AuthUtil.makeJsonMapRequest
import com.baulsupp.oksocial.authenticator.AuthUtil.uriGetRequest
import java.util.stream.Collectors.joining

object FacebookAuthFlow {

    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
              clientSecret: String,
              scopes: Set<String>): Oauth2Token {
        SimpleWebServer.forCode().use { s ->

            val serverUri = s.redirectUri

            val loginUrl = "https://www.facebook.com/dialog/oauth"
            +"?client_id=" + clientId
            +"&redirect_uri=" + serverUri
            +"&scope=" + URLEncoder.encode(scopes.stream().collect<String, *>(joining(",")),
                    "UTF-8")

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val tokenUrl = "https://graph.facebook.com/v2.10/oauth/access_token"
            +"?client_id=" + clientId
            +"&redirect_uri=" + serverUri
            +"&client_secret=" + clientSecret
            +"&code=" + code

            val map = makeJsonMapRequest(client, uriGetRequest(tokenUrl))

            val shortToken = map["access_token"] as String

            val exchangeUrl = "https://graph.facebook.com/oauth/access_token"
            +"?grant_type=fb_exchange_token"
            +"&client_id=" + clientId
            +"&client_secret=" + clientSecret
            +"&fb_exchange_token=" + shortToken

            val longTokenBody = makeJsonMapRequest(client, uriGetRequest(exchangeUrl))

            return Oauth2Token(longTokenBody["access_token"] as String)
        }
    }
}
