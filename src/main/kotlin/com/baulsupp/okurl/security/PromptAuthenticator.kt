package com.baulsupp.okurl.security

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.logging.Logger

object PromptAuthenticator : Authenticator {
  val logger = Logger.getLogger(PromptAuthenticator::class.java.name)!!

  override fun authenticate(route: Route?, response: Response): Request? {
    val request = response.request()

    val type = if (response.header("WWW-Authenticate") != null) "WWW" else "Proxy"

    if (type != null) {
      val challengeHeader = if (type == "WWW") response.header("WWW-Authenticate") else response.header("Proxy-Authenticate")
      val requestHeaderKey = if (type == "WWW") "Authorization" else "Proxy-Authorization"
      val requestHeader = request.header(requestHeaderKey)

      if (requestHeader != null) {
        logger.warning("Failed authenticate challenge")
        return null
      }

      if (challengeHeader == null) {
        logger.warning("No challenge found")
        return null
      }

      if (challengeHeader.startsWith("Basic ")) {
        println(challengeHeader)
        return request.newBuilder().header(requestHeaderKey, basicAuth()).build()
      } else if (challengeHeader.startsWith("Digest ")) {
        logger.warning("Digest not supported")
      } else if (challengeHeader.startsWith("Negotiate ")) {
        logger.warning("Kerberos not supported")
      } else {
        logger.warning("Unknown authentication request: $challengeHeader")
      }
    }

    return null
  }

  fun basicAuth() = Credentials.basic("a", "b")
}
