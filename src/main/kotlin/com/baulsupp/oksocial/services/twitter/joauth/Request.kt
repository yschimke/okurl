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

import java.util.Collections

interface Request {

  fun authHeader(): String
  fun body(): String
  fun contentType(): String
  fun host(): String
  fun method(): String?
  fun path(): String
  fun port(): Int
  fun queryString(): String
  fun scheme(): String?

  interface ParsedRequestFactory {
    fun parsedRequest(request: Request, params: List<Pair<String, String>>): ParsedRequest
  }

  class ParsedRequest(private val scheme: String?, private val host: String?, private val port: Int, private val verb: String?, private val path: String?, val params: List<Pair<String, String>>) {
    fun scheme(): String? {
      return scheme
    }

    fun host(): String? {
      return host
    }

    fun port(): Int {
      return port
    }

    fun verb(): String? {
      return verb
    }

    fun path(): String? {
      return path
    }

    fun params(): List<Pair<String, String>> {
      return params
    }

    override fun toString(): String {
      return "ParsedRequest{" +
              "scheme='" + scheme + '\'' +
              ", host='" + host + '\'' +
              ", port=" + port +
              ", verb='" + verb + '\'' +
              ", path='" + path + '\'' +
              ", params=" + params +
              '}'
    }

    override fun equals(o: Any?): Boolean {
      if (this === o) return true
      if (o == null || javaClass != o.javaClass) return false

      val that = o as ParsedRequest?

      if (port != that!!.port) return false
      if (if (host != null) host != that.host else that.host != null) return false
      if (if (params != null) params != that.params else that.params != null) return false
      if (if (path != null) path != that.path else that.path != null) return false
      if (if (scheme != null) scheme != that.scheme else that.scheme != null) return false
      return if (if (verb != null) verb != that.verb else that.verb != null) false else true

    }

    override fun hashCode(): Int {
      var result = scheme?.hashCode() ?: 0
      result = 31 * result + (host?.hashCode() ?: 0)
      result = 31 * result + port
      result = 31 * result + (verb?.hashCode() ?: 0)
      result = 31 * result + (path?.hashCode() ?: 0)
      result = 31 * result + (params?.hashCode() ?: 0)
      return result
    }
  }

  companion object {
    val factory: ParsedRequestFactory = object : ParsedRequestFactory {
      override fun parsedRequest(request: Request, params: List<Pair<String, String>>): ParsedRequest {
        return ParsedRequest(
                if (request.scheme() == null) null else request.scheme()!!.toUpperCase(),
                request.host(),
                request.port(),
                if (request.method() == null) null else request.method()!!.toUpperCase(),
                request.path(),
                params
        )
      }
    }
  }
}