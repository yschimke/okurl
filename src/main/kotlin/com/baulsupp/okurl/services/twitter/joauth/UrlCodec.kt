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

package com.baulsupp.okurl.services.twitter.joauth

import java.nio.charset.Charset
import kotlin.experimental.and

object UrlCodec {
  private const val UTF_8 = "UTF-8"
  private val UTF_8_CHARSET = Charset.forName(UTF_8)

  private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  private fun isUnreserved(c: Char): Boolean {
    return c in 'a'..'z' || c in 'A'..'Z' ||
      c in '0'..'9' || c == '.' || c == '-' || c == '_' || c == '~'
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
}
