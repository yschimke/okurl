package com.baulsupp.oksocial.services.twitter

import com.twitter.joauth.Normalizer
import com.twitter.joauth.OAuthParams
import com.twitter.joauth.Signer
import com.twitter.joauth.UrlCodec
import com.twitter.joauth.keyvalue.KeyValueHandler
import com.twitter.joauth.keyvalue.KeyValueParser
import okhttp3.FormBody
import okhttp3.Request
import okio.Buffer
import java.nio.charset.Charset
import java.security.SecureRandom
import java.time.Clock
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.logging.Level
import java.util.logging.Logger

class Signature @JvmOverloads constructor(private val clock: Clock = Clock.systemDefaultZone(),
        private val random: () -> Long = { SecureRandom().nextLong() }) {

  private fun quoted(str: String): String {
    return "\"" + str + "\""
  }

  private fun generateTimestamp(): Long {
    val timestamp = clock.millis()
    return timestamp / 1000
  }

  private fun generateNonce(): String {
    return java.lang.Long.toString(Math.abs(random())) + clock.millis()
  }

  fun generateAuthorization(request: Request, credentials: TwitterCredentials): String {
    val timestampSecs = generateTimestamp()
    val nonce = generateNonce()

    val normalizer = Normalizer.getStandardNormalizer()
    val signer = Signer.getStandardSigner()

    val oAuth1Params = OAuthParams.OAuth1Params(
            credentials.token, credentials.consumerKey, nonce, timestampSecs,
            java.lang.Long.toString(timestampSecs), "", OAuthParams.HMAC_SHA1,
            OAuthParams.ONE_DOT_OH
    )

    val javaParams = ArrayList<com.twitter.joauth.Request.Pair>()

    val queryParamNames = request.url().queryParameterNames()
    for (queryParam in queryParamNames) {
      val values = request.url().queryParameterValues(queryParam)

      values.mapTo(javaParams) {
        com.twitter.joauth.Request.Pair(UrlCodec.encode(queryParam),
                UrlCodec.encode(it))
      }
    }

    if (request.method() == "POST") {
      val body = request.body()

      if (body is FormBody) {
        val formBody = body as FormBody?

        (0 until formBody!!.size()).mapTo(javaParams) {
          com.twitter.joauth.Request.Pair(formBody.encodedName(it),
                  formBody.encodedValue(it))
        }
      } else if (isFormContentType(request)) {
        val buffer = Buffer()
        body!!.writeTo(buffer)
        val encodedBody = buffer.readString(Charset.forName("UTF-8"))

        val handler = KeyValueHandler.DuplicateKeyValueHandler()

        val bodyParser = KeyValueParser.StandardKeyValueParser("&", "=")
        bodyParser.parse(encodedBody, listOf<KeyValueHandler>(handler))

        javaParams.addAll(handler.toList())
      }
    }

    val normalized = normalizer.normalize(
            if (request.isHttps) "https" else "http", request.url().host(), request.url().port(),
            request.method(), request.url().encodedPath(), javaParams, oAuth1Params
    )

    log.log(Level.FINE, "normalised " + normalized)
    log.log(Level.FINE, "secret " + credentials.secret)
    log.log(Level.FINE, "consumerSecret " + credentials.consumerSecret)

    val signature = signer.getString(normalized, credentials.secret, credentials.consumerSecret)

    val oauthHeaders = LinkedHashMap<String, String>()
    if (credentials.consumerKey != null) {
      oauthHeaders.put(OAuthParams.OAUTH_CONSUMER_KEY, quoted(credentials.consumerKey!!))
    }
    oauthHeaders.put(OAuthParams.OAUTH_NONCE, quoted(nonce))
    oauthHeaders.put(OAuthParams.OAUTH_SIGNATURE, quoted(signature))
    oauthHeaders.put(OAuthParams.OAUTH_SIGNATURE_METHOD, quoted(OAuthParams.HMAC_SHA1))
    oauthHeaders.put(OAuthParams.OAUTH_TIMESTAMP, quoted(java.lang.Long.toString(timestampSecs)))
    if (credentials.token != null) {
      oauthHeaders.put(OAuthParams.OAUTH_TOKEN, quoted(credentials.token!!))
    }
    oauthHeaders.put(OAuthParams.OAUTH_VERSION, quoted(OAuthParams.ONE_DOT_OH))

    return "OAuth " + oauthHeaders.entries.joinToString(", ") { it.key + "=" + it.value }
  }

  private fun isFormContentType(request: Request): Boolean {
    return request.body()!!.contentType()!!.toString().startsWith(
            "application/x-www-form-urlencoded")
  }

  companion object {
    private val log = Logger.getLogger(TwitterAuthInterceptor::class.java.name)
  }
}
