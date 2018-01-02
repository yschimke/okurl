package com.baulsupp.oksocial.services.twitter.joauth

import com.baulsupp.oksocial.services.twitter.joauth.OAuthParams.AND
import com.baulsupp.oksocial.services.twitter.joauth.OAuthParams.HMACSHA1
import com.baulsupp.oksocial.services.twitter.joauth.OAuthParams.HMACSHA256
import okio.ByteString
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * the standard implementation of the Signer trait. Though stateless and threadsafe,
 * this is a class rather than an object to allow easy access from Java. Scala codebases
 * should use the corresponding STANDARD_SIGNER object instead.
 */
object StandardSigner {
  fun getString(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): String {
    return UrlCodec.encode(ByteString.of(*getBytes(str, signatureMethod, tokenSecret, consumerSecret)).base64())!!
  }

  private fun getBytes(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): ByteArray {
    val algorithm = getSignerAlgorithm(signatureMethod)
    val key = consumerSecret + AND + tokenSecret
    val signingKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), algorithm)

    //TODO: Mac looks thread safe, if not consider synchronizing this
    val mac = Mac.getInstance(algorithm)
    mac.init(signingKey)
    return mac.doFinal(str.toByteArray(StandardCharsets.UTF_8))
  }

  private fun getSignerAlgorithm(signatureMethod: String): String {
    return if ("HMAC-SHA256" == signatureMethod) HMACSHA256 else HMACSHA1
  }
}