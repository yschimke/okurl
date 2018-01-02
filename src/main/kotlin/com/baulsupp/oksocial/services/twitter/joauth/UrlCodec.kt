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

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.experimental.and

object UrlCodec {
  private val UTF_8 = "UTF-8"
  private val UTF_8_CHARSET = Charset.forName(UTF_8)

  //TODO: is this necessary? can we just call isUnreserved((char)b) ?
  private val PLUS = "+"
  private val ENCODED_PLUS = "%20"
  private val UNDERSCORE = "_"
  private val ENCODED_UNDERSCORE = "%5F"
  private val DASH = "-"
  private val ENCODED_DASH = "%2D"
  private val PERIOD = "."
  private val ENCODED_PERIOD = "%2E"
  private val TILDE = "~"
  private val ENCODED_TILDE = "%7E"
  private val COMMA = ","
  private val ENCODED_COMMA = "%2C"
  private val ENCODED_OPEN_BRACKET = "%5B"
  private val ENCODED_CLOSE_BRACKET = "%5D"

  private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  private fun isUnreserved(c: Char): Boolean {
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' ||
            c >= '0' && c <= '9' || c == '.' || c == '-' || c == '_' || c == '~'
  }

  fun encode(s: String?): String? {
    if (s == null) {
      return null
    }

    var sb: StringBuilder? = null

    var startingIndex = 0
    var hasReservedChars = false

    // scan through to see where we have to start % encoding, if at all
    while (startingIndex < s.length && !hasReservedChars) {
      if (!isUnreserved(s[startingIndex])) {
        hasReservedChars = true
      } else {
        startingIndex += 1
      }
    }

    if (hasReservedChars && startingIndex < s.length) {
      sb = StringBuilder(s.length + 40)
      sb.append(s.substring(0, startingIndex))

      val byteArray = s.substring(startingIndex).toByteArray(UTF_8_CHARSET)
      for (i in byteArray.indices) {
        val bite = byteArray[i]
        if (isUnreserved(bite.toChar())) {
          sb.append(bite.toChar())
        } else {
          // turn the Byte into an int into the hex string, but be sure to mask out the unneeded bits
          // to avoid nastiness with converting to a negative int
          sb.append('%')
          sb.append(HEX_DIGITS[bite.toInt() shr 4 and 0x0F])
          sb.append(HEX_DIGITS[(bite and 0x0F).toInt()])
        }
      }
    }

    return if (sb == null) s else sb.toString()
  }

  fun normalize(s: String?): String? {
    if (s == null) {
      return null
    }

    var sb: StringBuilder? = null
    val length = s.length
    var i = 0

    while (i < length) {
      val c = s[i]
      if (c == '%' || c == '+' || c == ',' || c == '[' || c == ']') {
        if (sb == null) {
          sb = StringBuilder(s.length + 40) //use length
          sb.append(s.substring(0, i))
        }
        if (c == '%') {
          if (i + 3 <= length) {
            if (ENCODED_UNDERSCORE.regionMatches(1, s, i + 1, 2, ignoreCase = true)) {
              sb.append(UNDERSCORE)
            } else if (ENCODED_DASH.regionMatches(1, s, i + 1, 2, ignoreCase = true)) {
              sb.append(DASH)
            } else if (ENCODED_TILDE.regionMatches(1, s, i + 1, 2, ignoreCase = true)) {
              sb.append(TILDE)
            } else if (ENCODED_PERIOD.regionMatches(1, s, i + 1, 2, ignoreCase = true)) {
              sb.append(PERIOD)
            } else {
              for (j in i until i + 3) {
                sb.append(Character.toUpperCase(s[j]))
              }
            }

            i += 2
          } else {
            sb.append(c)
          }
        } else if (c == ',') {
          sb.append(ENCODED_COMMA)
        } else if (c == '+') {
          sb.append(ENCODED_PLUS)
        } else if (c == '[') {
          sb.append(ENCODED_OPEN_BRACKET)
        } else if (c == ']') {
          sb.append(ENCODED_CLOSE_BRACKET)
        }
      } else if (sb != null) {
        sb.append(c)
      }
      i += 1
    }

    return if (sb == null) s else sb.toString()
  }

  @Throws(UnsupportedEncodingException::class)
  fun decode(s: String?): String? {
    return if (s == null) null else URLDecoder.decode(s, UrlCodec.UTF_8)
  }
}