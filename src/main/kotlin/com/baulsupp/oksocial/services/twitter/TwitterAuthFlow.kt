package com.baulsupp.oksocial.services.twitter

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.output.OutputHandler
import com.twitter.joauth.keyvalue.KeyValueHandler
import com.twitter.joauth.keyvalue.KeyValueParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

abstract class TwitterAuthFlow(protected val client: OkHttpClient, protected val outputHandler: OutputHandler<Response>) {

  suspend fun generateRequestToken(unauthed: TwitterCredentials, callback: String): TwitterCredentials {
    val body = FormBody.Builder().add("oauth_callback", callback).build()
    var request = Request.Builder().url("https://api.twitter.com/oauth/request_token")
        .post(body)
        .build()

    request = request.newBuilder()
        .header("Authorization",
            Signature().generateAuthorization(request, unauthed))
        .build()

    val response = AuthUtil.makeSimpleRequest(client, request)

    val tokenMap = parseTokenMap(response)
    return TwitterCredentials(unauthed.username, unauthed.consumerKey,
        unauthed.consumerSecret,
        tokenMap["oauth_token"], tokenMap["oauth_token_secret"])
  }

  suspend fun generateAccessToken(requestCredentials: TwitterCredentials,
                                    verifier: String): TwitterCredentials {
    val body = FormBody.Builder().add("oauth_verifier", verifier).build()
    var request = Request.Builder().url("https://api.twitter.com/oauth/access_token")
        .post(body)
        .build()

    request = request.newBuilder()
        .header("Authorization",
            Signature().generateAuthorization(request, requestCredentials))
        .build()

    val response = AuthUtil.makeSimpleRequest(client, request)

    val tokenMap = parseTokenMap(response)

    return TwitterCredentials(tokenMap["screen_name"], requestCredentials.consumerKey,
        requestCredentials.consumerSecret,
        tokenMap["oauth_token"], tokenMap["oauth_token_secret"])
  }

  @Throws(IOException::class)
  protected fun showUserLogin(newCredentials: TwitterCredentials) {
    outputHandler.openLink(
        "https://api.twitter.com/oauth/authenticate?oauth_token=" + newCredentials.token)
  }

  companion object {

    protected fun parseTokenMap(tokenDetails: String): Map<String, String> {
      val handler = KeyValueHandler.SingleKeyValueHandler()

      val bodyParser = KeyValueParser.StandardKeyValueParser("&", "=")
      bodyParser.parse(tokenDetails, listOf<KeyValueHandler>(handler))

      return handler.toMap()
    }
  }
}
