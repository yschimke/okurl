package com.baulsupp.okurl.services.symphony

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionMappings
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.postJsonBody
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.services.symphony.model.Stream
import com.baulsupp.okurl.services.symphony.model.StreamListRequest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class SymphonyUrlCompleter(
  val pods: List<String>,
  private val completionVariableCache: CompletionVariableCache,
  val client: OkHttpClient
) : ApiCompleter {
  override suspend fun prefixUrls(): UrlList {
    val urls = podsToHosts(pods).flatMap { listOf("https://$it", "https://$it/") }
    return UrlList(UrlList.Match.HOSTS, urls)
  }

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    val basePaths = UrlList.fromResource("symphony") ?: throw IllegalStateException("missing resource")
    val pod = hostPod(url.host())

    val mappings = CompletionMappings()
    mappings.withVariable("sid") {
      client.queryList<Stream>(request {
        url("https://$pod.symphony.com/pod/v1/streams/list")
        postJsonBody(StreamListRequest("IM", "MIM", "ROOM", "POST"))
      }).map { it.id }
    }
    mappings.withVariable("rid") {
      client.queryList<Stream>(request {
        url("https://$pod.symphony.com/pod/v1/streams/list")
        postJsonBody(StreamListRequest("ROOM"))
      }).map { it.id }
    }
    mappings.withVariable("uid") {
      client.queryList<Stream>(request {
        url("https://$pod.symphony.com/pod/v1/streams/list")
        postJsonBody(StreamListRequest("IM", "MIM", "ROOM", "POST"))
      }).flatMap { it.streamAttributes?.members?.map(Long::toString).orEmpty() }
    }

    val domainUrlLists = basePaths.replace("pod", listOf(pod), false)
    return mappings.replaceVariables(domainUrlLists)
  }

  fun hostPod(host: String): String {
    return host.replace("(-api)?.symphony.com$".toRegex(), "")
  }

//  fun withVariable(name: String, values: suspend () -> List<String>?) {
//    mappings.withVariable(name) { values().orEmpty() }
//  }
//
//  fun withCachedVariable(name: String, field: String, fn: suspend () -> List<String>?) {
//    withVariable(field) { completionVariableCache.compute(name, field, fn) }
//  }

  companion object {
    fun podsToHosts(pods: Iterable<String>): Set<String> =
      pods.flatMap { setOf("$it.symphony.com", "$it-api.symphony.com") }.toSet()
  }
}
