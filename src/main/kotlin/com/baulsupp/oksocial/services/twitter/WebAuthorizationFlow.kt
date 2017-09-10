package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import java.io.IOException

class WebAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<*>) : TwitterAuthFlow(client, outputHandler) {

    @Throws(IOException::class)
    fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
        SimpleWebServer({ it.getParameter("oauth_verifier") }).use({ s ->
            val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

            val requestCredentials = generateRequestToken(unauthed, s.redirectUri)

            showUserLogin(requestCredentials)

            val verifier = s.waitForCode()

            return generateAccessToken(requestCredentials, verifier)
        })
    }
}
