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

  /**
   * the singleton object of StandardOAuthParamsHelper
   */
  val STANDARD_OAUTH_PARAMS_HELPER: OAuthParamsHelper = StandardOAuthParamsHelperImpl()

  /**
   * pull all the OAuth parameter string constants into one place,
   * add a convenience method for determining if a string is an
   * OAuth 1.0 fieldname.
   */
  val BEARER_TOKEN = "Bearer"
  val CLIENT_ID = "client_id"
  val OAUTH_TOKEN = "oauth_token"
  val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
  val OAUTH_SIGNATURE = "oauth_signature"
  val OAUTH_NONCE = "oauth_nonce"
  val OAUTH_TIMESTAMP = "oauth_timestamp"
  val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
  val OAUTH_VERSION = "oauth_version"
  val NORMALIZED_REQUEST = "normalized_request"
  val UNSET = "(unset)"

  val HMAC_SHA1 = "HMAC-SHA1"
  val HMAC_SHA256 = "HMAC-SHA256"
  val ONE_DOT_OH = "1.0"
  val ONE_DOT_OH_A = "1.0a"

  val OAUTH1_HEADER_AUTHTYPE = "oauth"
  val OAUTH2_HEADER_AUTHTYPE = "bearer"


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

    fun nonce(): String {
      return nonce
    }

    fun timestampSecs(): Long? {
      return timestampSecs
    }

    fun timestampStr(): String {
      return timestampStr
    }

    fun signature(): String {
      return signature
    }

    fun signatureMethod(): String {
      return signatureMethod
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

  /**
   * A collector for OAuth and other params. There are convenience methods for determining
   * if it has all OAuth parameters set, just the token set, and for obtaining
   * a list of all params for use in producing the normalized request.
   */
  class OAuthParamsBuilder(private val helper: OAuthParamsHelper) {

    //todo: make this final
    var v2Token: String? = null
    var token: String? = null
    var consumerKey: String? = null
    var nonce: String? = null
    var timestampSecs: Long? = -1L
    var timestampStr: String? = null
    var signature: String? = null
    var signatureMethod: String? = null
    var version: String? = null

    private val paramsHandler = KeyValueHandler.DuplicateKeyValueHandler()
    private val otherOAuthParamsHandler = KeyValueHandler.SingleKeyValueHandler()

    var headerHandler: KeyValueHandler = object: KeyValueHandler {
      override fun handle(key: String, value: String) {
        handleKeyValue(key, value, true)
      }
    }

    var queryHandler: KeyValueHandler = object: KeyValueHandler {
      override fun handle(key: String, value: String) {
        handleKeyValue(key, value, false)
      }
    }

    val isOAuth2: Boolean
      get() = v2Token != null && !isOAuth1 && !isOAuth1TwoLegged

    val isOAuth1TwoLegged: Boolean
      get() = (token == null || "" == token) &&
              consumerKey != null &&
              nonce != null &&
              timestampStr != null &&
              signature != null &&
              signatureMethod != null

    // version is optional, so not included here
    val isOAuth1: Boolean
      get() = token != null &&
              "" != token &&
              consumerKey != null &&
              nonce != null &&
              timestampStr != null &&
              signature != null &&
              signatureMethod != null

    private fun notEmpty(value: String?): Boolean {
      return value != null && value != ""
    }

    private fun handleKeyValue(key: String, value: String?, fromHeader: Boolean) {

      // TODO: This needs clean up. replace the if/else with enum/map-lookup
      // Known keys can be in an enum, and parser can be updated to point to these keys, instead of creating a new key string.

      // empty values for these keys are swallowed
      if (BEARER_TOKEN == key) {
        if (fromHeader && notEmpty(value)) {
          v2Token = value
        }
      } else if (CLIENT_ID == key) {
        if (fromHeader && notEmpty(value)) {
          consumerKey = value
        }
      } else if (OAUTH_TOKEN == key) {
        if (value != null) {
          token = value.trim { it <= ' ' }
        }
      } else if (OAUTH_CONSUMER_KEY == key) {
        if (notEmpty(value)) {
          consumerKey = value
        }
      } else if (OAUTH_NONCE == key) {
        if (notEmpty(value)) {
          nonce = value
        }
      } else if (OAUTH_TIMESTAMP == key) {
        val timestamp = helper.parseTimestamp(value)
        if (timestamp != null) {
          timestampSecs = timestamp
          timestampStr = value
        }
      } else if (OAUTH_SIGNATURE == key) {
        if (notEmpty(value)) {
          signature = helper.processSignature(value!!)
        }
      } else if (OAUTH_SIGNATURE_METHOD == key) {
        if (notEmpty(value)) {
          signatureMethod = value
        }
      } else if (OAUTH_VERSION == key) {
        if (notEmpty(value)) {
          version = value
        }
      } else if (key.startsWith("oauth_")) {
        // send oauth_prefixed to a uniquekey handler
        otherOAuthParamsHandler.handle(key, value!!)
      } else {
        // send other params to the handler, but only if they didn't come from the header
        if (!fromHeader) paramsHandler.handle(key, value!!)
      }
    }

    // we use String.format here, because we're probably not that worried about
    // effeciency when printing the class for debugging
    override fun toString(): String {
      return String.format("%s=%s,%s=%s,%s=%s,%s=%s,%s=%s(->%s),%s=%s,%s=%s,%s=%s",
              BEARER_TOKEN, valueOrUnset(v2Token),
              OAUTH_TOKEN, valueOrUnset(token),
              OAUTH_CONSUMER_KEY, valueOrUnset(consumerKey),
              OAUTH_NONCE, valueOrUnset(nonce),
              OAUTH_TIMESTAMP, timestampStr, timestampSecs,
              OAUTH_SIGNATURE, valueOrUnset(signature),
              OAUTH_SIGNATURE_METHOD, valueOrUnset(signatureMethod),
              OAUTH_VERSION, valueOrUnset(version))
    }

    fun oAuth2Token(): String? {
      return v2Token
    }

    fun otherParams(): List<Pair<String, String>> {
      return paramsHandler.toList() + otherOAuthParamsHandler.toList()
    }

    // make an immutable params instance
    fun oAuth1Params(): OAuth1Params {
      return OAuth1Params(
              token,
              consumerKey!!,
              nonce!!,
              timestampSecs,
              timestampStr!!,
              signature!!,
              signatureMethod!!,
              version
      )
    }
  }

  interface OAuthParamsHelper {
    /**
     * allows one to override the default behavior when parsing timestamps,
     * which is to parse them as integers, and ignore timestamps that are
     * malformed
     */
    fun parseTimestamp(str: String?): Long?

    /**
     * allows custom processing of the OAuth 1.0 signature obtained from the request.
     */
    fun processSignature(str: String): String

    /**
     * allows custom processing of keys obtained from the request
     */
    fun processKey(str: String): String
  }

  /**
   * Provides the default implementation of the OAuthParamsHelper trait
   * Though stateless and threadsafe, this is a class rather than an object to allow easy
   * access from Java. Scala codebases should use the corresponding STANDARD_OAUTH_PARAMS_HELPER
   * object instead.
   */
  class StandardOAuthParamsHelperImpl : OAuthParamsHelper {

    override fun parseTimestamp(str: String?): Long? {
      try {
        return java.lang.Long.parseLong(str!!)
      } catch (e: Exception) {
        return null
      }

    }

    override fun processKey(str: String): String {
      return str
    }

    override fun processSignature(str: String): String {
      return str
    }
  }
}