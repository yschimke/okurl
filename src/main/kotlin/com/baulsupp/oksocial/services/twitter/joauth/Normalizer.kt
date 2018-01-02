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
import java.util.Collections
import java.util.Comparator

/**
 * a Normalizer takes the fields that describe an OAuth 1.0a request, and produces
 * the normalized string that is used for the signature.
 */
abstract class Normalizer {

  abstract fun normalize(
          scheme: String?,
          host: String?,
          port: Int,
          verb: String?,
          path: String?,
          params: List<Pair<String, String>>,
          oAuth1Params: OAuthParams.OAuth1Params
  ): String

  fun normalize(req: Request.ParsedRequest, oAuth1Params: OAuthParams.OAuth1Params): String {
    return normalize(
            req.scheme(),
            req.host(),
            req.port(),
            req.verb(),
            req.path(),
            req.params(),
            oAuth1Params
    )
  }

  /**
   * the standard implementation of the Normalizer trait. Though stateless and threadsafe,
   * this is a class rather than an object to allow easy access from Java. Scala codebases
   * should use the corresponding STANDARD_NORMALIZER object instead.
   */
  class StandardNormalizer : Normalizer() {

    /* TODO: there is no way to clear string builder in java. see what can be done here.
     Not using thread local.

    private static final ThreadLocal<StringBuilder> builders = new ThreadLocal<StringBuilder>() {
      @Override
      protected StringBuilder initialValue() {
        return new StringBuilder(512);
      }
    };
    */

    override fun normalize(
            scheme: String?,
            host: String?,
            port: Int,
            verb: String?,
            path: String?,
            params: List<Pair<String, String>>,
            oAuth1Params: OAuthParams.OAuth1Params
    ): String {

      // We only need the stringbuilder for the duration of this method
      val paramsBuilder = StringBuilder(512)

      // first, concatenate the params and the oAuth1Params together.
      // the parameters are already URLEncoded, so we leave them alone
      val sigParams = mutableListOf<Pair<String, String>>()
      sigParams.addAll(params)
      sigParams.addAll(oAuth1Params.toList(false))

      // TODO proper sort
      sigParams.sortBy { it.first + ":" + it.second }

      if (!sigParams.isEmpty()) {
        val head = sigParams[0]
        paramsBuilder.append(head.first).append('=').append(head.second)
        for (i in 1 until sigParams.size) {
          val pair = sigParams[i]
          paramsBuilder.append('&').append(pair.first).append('=').append(pair.second)
        }
      }

      val requestUrlBuilder = StringBuilder(512)
      requestUrlBuilder.append(scheme!!.toLowerCase())
      requestUrlBuilder.append("://")
      requestUrlBuilder.append(host!!.toLowerCase())
      if (includePortString(port, scheme)) {
        requestUrlBuilder.append(":").append(port)
      }
      requestUrlBuilder.append(path)

      val normalizedBuilder = StringBuilder(512)

      normalizedBuilder.append(verb!!.toUpperCase())
      normalizedBuilder.append('&').append(UrlCodec.encode(requestUrlBuilder.toString()))
      normalizedBuilder.append('&').append(UrlCodec.encode(paramsBuilder.toString()))

      return normalizedBuilder.toString()
    }

    /**
     * The OAuth 1.0a spec says that the port should not be included in the normalized string
     * when (1) it is port 80 and the scheme is HTTP or (2) it is port 443 and the scheme is HTTPS
     */
    internal fun includePortString(port: Int, scheme: String): Boolean {
      return !(port == 80 && HTTP.equals(scheme, ignoreCase = true) || port == 443 && HTTPS.equals(scheme, ignoreCase = true))
    }
  }

  companion object {

    internal val HTTP = "HTTP"
    internal val HTTPS = "HTTPS"

    private val STANDARD_NORMALIZER = StandardNormalizer()

    val standardNormalizer: Normalizer
      get() = STANDARD_NORMALIZER
  }
}