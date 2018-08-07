package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.oksocial.output.OutputHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

class DiscoveryApiDocPresenter(val registry: DiscoveryRegistry) : ApiDocPresenter {

  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) {
    val discoveryPaths = DiscoveryIndex.instance.getDiscoveryUrlForPrefix(url)

    val docs = discoveryPaths.map { p ->
      async(CommonPool) {
        withTimeout(5, TimeUnit.SECONDS) { registry.load(p, tokenSet) }
      }
    }.mapNotNull {
      try {
        it.await()
      } catch (ce: CancellationException) {
        // TODO log
        null
      }
    }

    val exactMatch = docs.firstOrNull { x -> matches(url, x) }

    val bestDoc = if (exactMatch != null) {
      exactMatch
    } else {
      // requested url may be a substring of longest baseUrl
      // assume that this means that single unique service owns this base url
      val best = docs.filter { service ->
        url.startsWith(service.baseUrl)
      }.maxBy { dd -> dd.baseUrl.length }

      if (best != null) {
        best
      } else {
        // multiple services sharing baseurl - return first
        outputHandler.info("Multiple services for path $url")
        null
      }
    }

    if (bestDoc != null) {
      outputHandler.info("name: " + bestDoc.apiName)
      outputHandler.info("docs: " + bestDoc.docLink)

      val e = bestDoc.findEndpoint(url)

      if (e != null) {
        outputHandler.info("endpoint id: " + e.id())
        outputHandler.info("url: " + e.url())
        outputHandler.info("scopes: " + e.scopeNames().joinToString(", "))
        outputHandler.info("")
        outputHandler.info(e.description())
        outputHandler.info("")
        e.parameters().forEach { p ->
          outputHandler.info("parameter: " + p.name() + " (" + p.type() + ")")
          p.description()?.let(outputHandler::info)
        }
      } else {
        outputHandler.info("base: " + bestDoc.baseUrl)
      }
    } else {
      outputHandler.info("No specific API found")
      outputHandler.info("https://developers.google.com/apis-explorer/#p/")
    }
  }

  private fun matches(url: String, x: DiscoveryDocument): Boolean {
    val eps = x.endpoints

    return eps.any { it.matches(url) }
  }
}
