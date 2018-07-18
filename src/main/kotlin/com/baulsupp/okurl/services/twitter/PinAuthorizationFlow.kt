package com.baulsupp.okurl.services.twitter

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.readPasswordString
import okhttp3.OkHttpClient
import okhttp3.Response

class PinAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<Response>) :
  TwitterAuthFlow(client, outputHandler) {

  private suspend fun promptForPin(newCredentials: TwitterCredentials): String {
    System.err.println(
      "Authorise by entering the PIN through a web browser")

    showUserLogin(newCredentials)

    return System.console().readPasswordString("Enter PIN: ")
  }

  suspend fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
    val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

    val requestCredentials = generateRequestToken(unauthed, "oob")

    val pin = promptForPin(requestCredentials)

    return generateAccessToken(requestCredentials, pin)
  }
}
