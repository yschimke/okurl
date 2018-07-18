package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.services.google.model.DiscoveryDoc
import com.baulsupp.okurl.services.google.model.Resource

/**
 * https://developers.google.com/discovery/v1/using
 */
class DiscoveryDocument(map: DiscoveryDoc) {
  val baseUrl = "" + map.rootUrl + map.servicePath

  val endpoints = expandEndpoints(map.resources)

  val urls = endpoints.map { e -> e.url() }.distinct()

  private fun expandEndpoints(resources: Map<String, Resource>?): List<DiscoveryEndpoint> {
    if (resources == null) {
      return listOf()
    }

    return resources.values.flatMap { r ->
      r.methods.orEmpty().values.map { m ->
        DiscoveryEndpoint(baseUrl, m)
      } + expandEndpoints(r.resources)
    }
  }

  val apiName = map.title

  val docLink = map.documentationLink

  fun findEndpoint(url: String) = endpoints.filter { e ->
    matches(url, e)
  }.sortedBy { it.httpMethod() != "GET" }.firstOrNull()

  private fun matches(url: String, e: DiscoveryEndpoint) = e.url() == url || e.matches(url)

  companion object {
    fun parse(definition: String): DiscoveryDocument =
      DiscoveryDocument(moshi.adapter(DiscoveryDoc::class.java).fromJson(definition)!!)
  }
}
