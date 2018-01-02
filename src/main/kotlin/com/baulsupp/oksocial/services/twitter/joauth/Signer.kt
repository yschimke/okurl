// Copyright 2011 Twitter, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License. You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.baulsupp.oksocial.services.twitter.joauth

import okio.ByteString
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A Signer takes a string, a token secret and a consumer secret, and produces a signed string
 */
abstract class Signer {

  /**
   * produce an encoded signature string
   */
  fun getString(str: String, tokenSecret: String, consumerSecret: String): String {
    return getString(str, OAuthParams.HMAC_SHA1, tokenSecret, consumerSecret)
  }

  /**
   * produce an encoded signature string
   */
  abstract fun getString(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): String

  /**
   * produce a signature as a byte array
   */
  abstract fun getBytes(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): ByteArray

  /**
   * decode an existing signature to a byte array
   */
  abstract fun toBytes(signature: String): ByteArray

  /**
   * the standard implementation of the Signer trait. Though stateless and threadsafe,
   * this is a class rather than an object to allow easy access from Java. Scala codebases
   * should use the corresponding STANDARD_SIGNER object instead.
   */
  class StandardSigner : Signer() {
    override fun getString(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): String {

      return UrlCodec.encode(ByteString.of(*getBytes(str, signatureMethod, tokenSecret, consumerSecret)).base64())!!
    }

    override fun getBytes(str: String, signatureMethod: String, tokenSecret: String, consumerSecret: String): ByteArray {

      val algorithm = getSignerAlgorithm(signatureMethod)
      val key = consumerSecret + AND + tokenSecret
      val signingKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), algorithm)

      //TODO: Mac looks thread safe, if not consider synchronizing this
      val mac = Mac.getInstance(algorithm)
      mac.init(signingKey)
      return mac.doFinal(str.toByteArray(StandardCharsets.UTF_8))
    }

    override fun toBytes(signature: String): ByteArray {
      return ByteString.decodeBase64(UrlCodec.decode(signature)!!.trim { it <= ' ' })!!.toByteArray()
    }

    private fun getSignerAlgorithm(signatureMethod: String): String {
      return if ("HMAC-SHA256" == signatureMethod) HMACSHA256 else HMACSHA1
    }

    companion object {
      private val AND = "&" //TODO: move to Normalizer
      private val HMACSHA1 = "HmacSHA1"
      private val HMACSHA256 = "HmacSHA256"
    }
  }

}