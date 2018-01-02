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

import java.util.ArrayList

object OAuthParams {

  val OAUTH_TOKEN = "oauth_token"
  val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
  val OAUTH_SIGNATURE = "oauth_signature"
  val OAUTH_NONCE = "oauth_nonce"
  val OAUTH_TIMESTAMP = "oauth_timestamp"
  val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
  val OAUTH_VERSION = "oauth_version"
  private val UNSET = "(unset)"

  val HMAC_SHA1 = "HMAC-SHA1"
  val ONE_DOT_OH = "1.0"

  private fun valueOrUnset(value: String?): String {
    return value ?: UNSET
  }

  /**
   * OAuth1Params is mostly just a container for OAuth 1.0a parameters.
   * The token is optional to allow for OAuth 1.0 two-legged requests.
   */
  class OAuth1Params(
          private val token: String?,
          private val consumerKey: String,
          private val nonce: String,
          private val timestampSecs: Long?,
          private val timestampStr: String,
          private val signature: String,
          private val signatureMethod: String,
          private val version: String?
  ) {

    fun token(): String? {
      return token
    }

    fun consumerKey(): String {
      return consumerKey
    }

    fun signature(): String {
      return signature
    }

    fun version(): String? {
      return version
    }

    fun toList(includeSig: Boolean): List<Pair<String, String>> {
      val buf = ArrayList<Pair<String, String>>()

      buf.add(Pair(OAUTH_CONSUMER_KEY, consumerKey))
      buf.add(Pair(OAUTH_NONCE, nonce))
      if (token != null) buf.add(Pair(OAUTH_TOKEN, token))
      if (includeSig) buf.add(Pair(OAUTH_SIGNATURE, signature))
      buf.add(Pair(OAUTH_SIGNATURE_METHOD, signatureMethod))
      buf.add(Pair(OAUTH_TIMESTAMP, timestampStr))
      if (version != null) buf.add(Pair(OAUTH_VERSION, version))

      return buf
    }

    // we use String.format here, because we're probably not that worried about
    // effeciency when printing the class for debugging
    override fun toString(): String {
      return String.format("%s=%s,%s=%s,%s=%s,%s=%s(->%s),%s=%s,%s=%s,%s=%s",
              OAUTH_TOKEN, valueOrUnset(token),
              OAUTH_CONSUMER_KEY, valueOrUnset(consumerKey),
              OAUTH_NONCE, valueOrUnset(nonce),
              OAUTH_TIMESTAMP, timestampStr, timestampSecs,
              OAUTH_SIGNATURE, valueOrUnset(signature),
              OAUTH_SIGNATURE_METHOD, valueOrUnset(signatureMethod),
              OAUTH_VERSION, valueOrUnset(version))
    }
  }

}