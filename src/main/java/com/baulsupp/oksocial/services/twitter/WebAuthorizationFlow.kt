package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.function.Function
import javax.servlet.http.HttpServletRequest
import okhttp3.OkHttpClient

class WebAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<*>) : TwitterAuthFlow(client, outputHandler) {

    @Throws(IOException::class)
    fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
        val codeReader = { r -> r.getParameter("oauth_verifier") }

        SimpleWebServer(codeReader).use({ s ->
            val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

            val requestCredentials = generateRequestToken(unauthed, s.redirectUri)

            showUserLogin(requestCredentials)

            val verifier = s.waitForCode()

            return generateAccessToken(requestCredentials, verifier)
        })
    }
}
