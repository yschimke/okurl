package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class PinAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<Response>) : TwitterAuthFlow(client, outputHandler) {

  private suspend fun promptForPin(newCredentials: TwitterCredentials): String {
    System.err.println(
        "Authorise by entering the PIN through a web browser")

    showUserLogin(newCredentials)

    // TODO move to IO pool
    return String(System.console().readPassword("Enter PIN: "))
  }

  suspend fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
    val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

    val requestCredentials = generateRequestToken(unauthed, "oob")

    val pin = promptForPin(requestCredentials)

    return generateAccessToken(requestCredentials, pin)
  }
}
