package com.baulsupp.okurl.security

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.net.HttpURLConnection.HTTP_PROXY_AUTH
import java.util.logging.Logger

class BasicPromptAuthenticator(val credentials: com.burgstaller.okhttp.digest.Credentials) : Authenticator {
  val logger = Logger.getLogger(BasicPromptAuthenticator::class.java.name)!!

  override fun authenticate(route: Route?, response: Response): Request? {
    val request = response.request()
    val challenge = response.challenges().find { it.scheme() == "Basic" }
      ?: throw IOException("No Basic Challenge found")
    val proxy = response.code() == HTTP_PROXY_AUTH

    val responseHeaderKey = if (proxy) "Proxy-Authenticate" else "WWW-Authenticate"
    val requestHeaderKey = if (proxy) "Proxy-Authorization" else "Authorization"

    val challengeHeader = response.header(responseHeaderKey)
    val requestHeader = request.header(requestHeaderKey)

    if (requestHeader != null) {
      logger.warning("Failed authenticate challenge")
      return null
    }

    if (challengeHeader == null) {
      logger.warning("No challenge found")
      return null
    }

    return request.newBuilder().header(requestHeaderKey, basicAuth()).build()
  }

  fun basicAuth(): String {
    return Credentials.basic(credentials.userName!!, credentials.password!!)
  }
}
