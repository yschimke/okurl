package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import java.io.IOException

class PinAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<*>) : TwitterAuthFlow(client, outputHandler) {

    @Throws(IOException::class)
    protected fun promptForPin(newCredentials: TwitterCredentials): String {
        System.err.println(
                "Authorise by entering the PIN through a web browser")

        showUserLogin(newCredentials)

        return String(System.console().readPassword("Enter PIN: "))
    }

    @Throws(IOException::class)
    fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
        val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

        val requestCredentials = generateRequestToken(unauthed, "oob")

        val pin = promptForPin(requestCredentials)

        return generateAccessToken(requestCredentials, pin)
    }
}
