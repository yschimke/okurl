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

/**
 * The Transformer trait describes the transformation function
 * from a string to a derived string
 */
interface Transformer {
  fun transform(input: String): String


  /**
   * The TrimTransformer trims the string
   */
  class TrimTransformer : Transformer {
    override fun transform(input: String): String {
      return input.trim { it <= ' ' }
    }
  }

  /**
   * The UrlEncodingNormalizingTransformer capitializes all of the
   * URLEncoded entities in a string, replaces +'s with %20s, and
   * un-encodes dashes and underscores. It will do strange things to
   * a string that is not actually URLEncoded.
   */
  class UrlEncodingNormalizingTransformer : Transformer {
    override fun transform(input: String): String {
      return UrlCodec.normalize(input)!!
    }
  }

  companion object {

    val TRIM_TRANSFORMER: Transformer = TrimTransformer()
    val URL_ENCODING_NORMALIZING_TRANSFORMER: Transformer = UrlEncodingNormalizingTransformer()
  }
}