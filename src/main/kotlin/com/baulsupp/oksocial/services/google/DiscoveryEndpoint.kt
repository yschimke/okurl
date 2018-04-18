package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.services.google.model.Method
import java.util.regex.Pattern

class DiscoveryEndpoint(private val baseUrl: String, private val method: Method) {

  fun id(): String = method.id

  fun url(): String = baseUrl + path()

  private fun path(): String = method.path

  fun description(): String = method.description ?: ""

  fun scopeNames(): List<String> = method.scopes ?: listOf()

  fun parameters(): List<DiscoveryParameter> = method.parameters?.map { (k, v) -> DiscoveryParameter(k, v) } ?: listOf()

  fun matches(requestUrl: String): Boolean {
    if (!requestUrl.startsWith(baseUrl)) {
      return false
    }

    val requestUrlPath = requestUrl.substring(baseUrl.length)

    return buildDocPathRegex().matcher(requestUrlPath).matches()
  }

  private fun buildDocPathRegex(): Pattern {
    val parameters = parameters()

    var hasQueryParams = false

    var pathPattern = this.path()

    for (p in parameters) {
      if (p.location() == "path") {
        var pPattern: String? = p.pattern()
        if (pPattern == null) {
          pPattern = ".*"
        } else if (pPattern.matches("\\^.*\\$".toRegex())) {
          pPattern = pPattern.substring(1, pPattern.length - 1)
        }
        val x = "\\{\\+?" + p.name() + "\\}"
        pathPattern = pathPattern.replace(x.toRegex(), pPattern)
      } else if (p.location() == "query") {
        hasQueryParams = true
      }
    }

    return Pattern.compile(pathPattern + if (hasQueryParams) "(\\?.*)?" else "")
  }

  fun httpMethod(): String = method.httpMethod ?: "GET"
}
