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
 * The KeyValueParser trait describes a parser that takes a String and a Seq[KeyValueHandler],
 * and calls each handler for each key/value pair encountered in the parsed String
 */
interface KeyValueParser {
  fun parse(input: String, handlers: List<KeyValueHandler>)

  /**
   * StandardKeyValueParser is a KeyValueParser that splits a string on a delimiter,
   * and then splits each pair with the kvDelimiter. both delimiters can be java-style
   * regular expressions.
   */
  class StandardKeyValueParser(private val delimiter: String, private val kvDelimiter: String) : KeyValueParser {

    override fun parse(input: String, handlers: List<KeyValueHandler>) {
      if (empty(input)) return

      val tokens = input.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

      tokens
              .map { token -> token.split(kvDelimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
              .forEach {
                when (it.size) {
                  2 -> if (!empty(it[0])) {
                    for (handler in handlers) handler.handle(it[0], it[1])
                  }
                  1 -> if (!empty(it[0])) {
                    for (handler in handlers) handler.handle(it[0], "")
                  }
                  else -> {
                  }
                }
              }
    }

    private fun empty(str: String?): Boolean {
      return str == null || str.isEmpty()
    }
  }
}