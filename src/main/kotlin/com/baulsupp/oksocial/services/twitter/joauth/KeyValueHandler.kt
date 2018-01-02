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

import java.util.*

/**
 * KeyValueHandler is a trait for a callback with a key and a value.
 * What you do with the key and value are up to you.
 */
interface KeyValueHandler {
  fun handle(key: String, value: String)

  class NullKeyValueHandler : KeyValueHandler {
    override fun handle(key: String, value: String) {}
  }

  /**
   * DuplicateKeyValueHandler produces a List[(String, String)] of key
   * value pairs, allowing duplicate values for keys.
   */
  class DuplicateKeyValueHandler : KeyValueHandler {
    private val buffer = ArrayList<Pair<String, String>>()

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
    private val kv = LinkedHashMap<String, String>()

    override fun handle(key: String, value: String) {
      kv.put(key, value)
    }

    fun toMap(): Map<String, String> {
      return kv
    }

    fun toList(): List<Pair<String, String>> {
      val iterator = kv.entries.iterator()
      val list = ArrayList<Pair<String, String>>(kv.size)

      while (iterator.hasNext()) {
        val next = iterator.next()
        list.add(Pair(next.key, next.value))
      }

      return list
    }
  }

  class MaybeQuotedValueKeyValueHandler(private val underlying: KeyValueHandler) : KeyValueHandler {

    override fun handle(key: String, value: String) {
      val trimmed = value.trim { it <= ' ' }
      if (trimmed.length > 1 && trimmed[0] == '"' && trimmed[trimmed.length - 1] == '"') {
        underlying.handle(key, trimmed.substring(1, trimmed.length - 1))
      } else {
        underlying.handle(key, value)
      }
    }
  }

  /**
   * PrintlnKeyValueHandler is very nice for debugging!
   * Pass it in to the Unpacker to see what's going on.
   */
  class PrintlnKeyValueHandler(private val prefix: String) : KeyValueHandler {

    override fun handle(key: String, value: String) {
      println(String.format("%s%s=%s", prefix, key, value))
    }
  }


  /**
   * TransformingKeyValueHandler applies the Transformers to
   * their respective key and value before passing along to the
   * underlying KeyValueHandler
   */
  open class TransformingKeyValueHandler(protected val underlying: KeyValueHandler, protected val keyTransformer: Transformer?, protected val valueTransformer: Transformer?) : KeyValueHandler {

    override fun handle(key: String, value: String) {
      underlying.handle(keyTransformer!!.transform(key), valueTransformer!!.transform(value))
    }
  }

  /**
   * TrimmingKeyValueHandler trims the key and value before
   * passing them to the underlying KeyValueHandler
   */
  class TrimmingKeyValueHandler(underlying: KeyValueHandler) : TransformingKeyValueHandler(underlying, Transformer.TRIM_TRANSFORMER, Transformer.TRIM_TRANSFORMER)

  /**
   * KeyTransformingKeyValueHandler applies a Transformer to the key
   * before passing the key value pair to the underlying KeyValueHandler
   */
  class KeyTransformingKeyValueHandler(underlying: KeyValueHandler, keyTransformer: Transformer) : TransformingKeyValueHandler(underlying, keyTransformer, null) {

    override fun handle(key: String, value: String) {
      underlying.handle(keyTransformer!!.transform(key), value)
    }
  }

  /**
   * ValueTransformingKeyValueHandler applies a Transformer to the value
   * before passing the key value pair to the underlying KeyValueHandler
   */
  class ValueTransformingKeyValueHandler(underlying: KeyValueHandler, valueTransformer: Transformer) : TransformingKeyValueHandler(underlying, null, valueTransformer) {

    override fun handle(key: String, value: String) {
      underlying.handle(key, valueTransformer!!.transform(value))
    }
  }

  /**
   * UrlEncodingNormalizingKeyValueHandler normalizes URLEncoded
   * keys and values, to properly capitalize them
   */
  class UrlEncodingNormalizingKeyValueHandler(underlying: KeyValueHandler) : TransformingKeyValueHandler(underlying, Transformer.URL_ENCODING_NORMALIZING_TRANSFORMER, Transformer.URL_ENCODING_NORMALIZING_TRANSFORMER)


  /**
   * key is set iff the handler was invoked exactly once with an empty value
   *
   * Note: this class is not thead safe
   */
  class OneKeyOnlyKeyValueHandler : KeyValueHandler {
    private var invoked = false
    var key: String? = null
      private set

    override fun handle(key: String, value: String) {
      if (invoked) {
        if (this.key != null) this.key = null
      } else {
        invoked = true //TODO: bug? should invoked be set to true, if _key is not set?
        if (value == "") this.key = key
      }
    }
  }
}