package com.baulsupp.okurl.security

import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import okhttp3.Authenticator
import okhttp3.Challenge
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.net.HttpURLConnection.HTTP_PROXY_AUTH
import java.util.logging.Logger

class BasicPromptAuthenticator(val credentials: BasicCredentials? = null) : Authenticator {
  val logger = Logger.getLogger(BasicPromptAuthenticator::class.java.name)!!

  override fun authenticate(route: Route?, response: Response): Request? {
    val request = response.request()
    val challenge = response.challenges().find { it.scheme == "Basic" }
      ?: throw IOException("No Basic Challenge found")
    val proxy = response.code() == HTTP_PROXY_AUTH

    val requestHeaderKey = if (proxy) "Proxy-Authorization" else "Authorization"

    val requestHeader = request.header(requestHeaderKey)

    if (requestHeader != null) {
      logger.warning("Failed authenticate challenge")
      return null
    }

    val basicCredentials = basicAuth(request, challenge)

    if (basicCredentials != null) {
      return request.newBuilder().header(requestHeaderKey, basicCredentials.header()).build()
    }

    return null
  }

  fun basicAuth(request: Request, challenge: Challenge): BasicCredentials? {
    val requestCredentials = (request.tag(Token::class.java) as? TokenValue)?.token as? BasicCredentials

    if (requestCredentials != null) {
      return requestCredentials
    }

    if (credentials != null) {
      return credentials
    }

    if (System.console() != null) {
      System.console().printf("Basic Auth (${challenge.realm()})\n")
      val user = System.console().readLine("User: ")
      val password = System.console().readPassword("Password: ")

      return BasicCredentials(user, String(password))
    }

    return null
  }
}
