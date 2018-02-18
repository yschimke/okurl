package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.OkHttpClient
import okhttp3.Response

class WebAuthorizationFlow(client: OkHttpClient, outputHandler: OutputHandler<Response>) :
  TwitterAuthFlow(client, outputHandler) {

  suspend fun authorise(consumerKey: String, consumerSecret: String): TwitterCredentials {
    SimpleWebServer({
      it.queryParameter("oauth_verifier")!!
    }).use({ s ->
      val unauthed = TwitterCredentials(null, consumerKey, consumerSecret, null, "")

      val requestCredentials = generateRequestToken(unauthed, s.redirectUri)

      showUserLogin(requestCredentials)

      val verifier = s.waitForCode()

      return generateAccessToken(requestCredentials, verifier)
    })
  }
}
