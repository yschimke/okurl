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
 * KeyValueHandler is a trait for a callback with a key and a value.
 * What you do with the key and value are up to you.
 */
interface KeyValueHandler {
  fun handle(key: String, value: String)

  /**
   * DuplicateKeyValueHandler produces a List[(String, String)] of key
   * value pairs, allowing duplicate values for keys.
   */
  class DuplicateKeyValueHandler : KeyValueHandler {
    private val buffer = mutableListOf<Pair<String, String>>()

    override fun handle(key: String, value: String) {
      buffer.add(Pair(key, value))
    }

    fun toList(): List<Pair<String, String>> {
      return buffer
    }
  }

  /**
   * SingleKeyValueHandler produces either a List[(String, String)]
   * or a Map[String, String] of key/value pairs, and will override
   * duplicate values for keys, using the last value encountered
   */
  class SingleKeyValueHandler : KeyValueHandler {
    private val kv = linkedMapOf<String, String>()

    override fun handle(key: String, value: String) {
      kv.put(key, value)
    }

    fun toMap(): Map<String, String> {
      return kv
    }

    fun toList(): List<Pair<String, String>> {
      val iterator = kv.entries.iterator()
      val list = mutableListOf<Pair<String, String>>()

      while (iterator.hasNext()) {
        val next = iterator.next()
        list.add(Pair(next.key, next.value))
      }

      return list
    }
  }
}