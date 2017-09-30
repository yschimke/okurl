package com.baulsupp.oksocial.services.foursquare

import com.baulsupp.oksocial.authenticator.AuthUtil.makeJsonMapRequest
import com.baulsupp.oksocial.authenticator.AuthUtil.uriGetRequest
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URLEncoder

object FourSquareAuthFlow {

    @Throws(IOException::class)
    fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
              clientSecret: String): Oauth2Token {
        SimpleWebServer.forCode().use { s ->

            val serverUri = s.redirectUri

            val loginUrl = "https://foursquare.com/oauth2/authenticate?client_id=$clientId&redirect_uri=${URLEncoder.encode(serverUri, "UTF-8")}&response_type=code"

            outputHandler.openLink(loginUrl)

            val code = s.waitForCode()

            val tokenUrl = "https://foursquare.com/oauth2/access_token?client_id=$clientId&client_secret=$clientSecret&grant_type=authorization_code&redirect_uri=${URLEncoder.encode(serverUri, "UTF-8")}&code=$code"

            val responseMap = makeJsonMapRequest(client, uriGetRequest(tokenUrl))

            return Oauth2Token(responseMap["access_token"] as String)
        }
    }
}
