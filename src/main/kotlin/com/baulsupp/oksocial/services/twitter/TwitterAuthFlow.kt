package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.kotlin.queryForString
import com.baulsupp.oksocial.kotlin.requestBuilder
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.services.twitter.joauth.KeyValueHandler
import com.baulsupp.oksocial.services.twitter.joauth.Signature
import com.baulsupp.oksocial.services.twitter.joauth.StandardKeyValueParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response

abstract class TwitterAuthFlow(protected val client: OkHttpClient,
                               protected val outputHandler: OutputHandler<Response>) {

  suspend fun generateRequestToken(unauthed: TwitterCredentials,
                                   callback: String): TwitterCredentials {
    val body = FormBody.Builder().add("oauth_callback", callback).build()
    var request = requestBuilder("https://api.twitter.com/oauth/request_token",
      NoToken)
      .post(body)
      .build()

    request = request.newBuilder()
      .header("Authorization",
        Signature().generateAuthorization(request, unauthed))
      .build()

    val tokenMap = parseTokenMap(client.queryForString(request))

    return TwitterCredentials(unauthed.username, unauthed.consumerKey,
      unauthed.consumerSecret,
      tokenMap["oauth_token"], tokenMap["oauth_token_secret"])
  }

  suspend fun generateAccessToken(requestCredentials: TwitterCredentials,
                                  verifier: String): TwitterCredentials {
    val body = FormBody.Builder().add("oauth_verifier", verifier).build()
    var request = requestBuilder("https://api.twitter.com/oauth/access_token",
      NoToken)
      .post(body)
      .build()

    request = request.newBuilder()
      .header("Authorization",
        Signature().generateAuthorization(request, requestCredentials))
      .build()

    val tokenMap = parseTokenMap(client.queryForString(request))

    return TwitterCredentials(tokenMap["screen_name"], requestCredentials.consumerKey,
      requestCredentials.consumerSecret,
      tokenMap["oauth_token"], tokenMap["oauth_token_secret"])
  }

  protected suspend fun showUserLogin(newCredentials: TwitterCredentials) {
    outputHandler.openLink(
      "https://api.twitter.com/oauth/authenticate?oauth_token=${newCredentials.token}")
  }

  protected fun parseTokenMap(tokenDetails: String): Map<String, String> {
    val handler = KeyValueHandler.SingleKeyValueHandler()

    val bodyParser = StandardKeyValueParser("&", "=")
    bodyParser.parse(tokenDetails, listOf<KeyValueHandler>(handler))

    return handler.toMap()
  }
}
