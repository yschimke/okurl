package com.baulsupp.okurl.services.google

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit.SECONDS
import java.util.logging.Level
import java.util.logging.Logger

class DiscoveryApiDocPresenter(val registry: DiscoveryRegistry) : ApiDocPresenter {
  private val logger = Logger.getLogger(DiscoveryApiDocPresenter::class.java.name)

  override suspend fun explainApi(
    url: String,
    outputHandler: OutputHandler<Response>,
    client: OkHttpClient,
    tokenSet: Token
  ) = supervisorScope {
    val discoveryPaths = DiscoveryIndex.instance.getDiscoveryUrlForPrefix(url)

    val docs = discoveryPaths.map { p ->
      async {
        withTimeout(SECONDS.toMillis(5)) { registry.load(p, tokenSet) }
      }
    }.mapNotNull {
      try {
        it.await()
      } catch (ce: ClientException) {
        logger.log(Level.FINE, "Failed to fetch discovery document", ce)
        null
      } catch (ce: CancellationException) {
        logger.log(Level.FINE, "Failed to fetch discovery document in timeout")
        null
      }
    }

    val exactMatch = docs.firstOrNull { x -> matches(url, x) }

    val bestDoc = if (exactMatch != null) {
      exactMatch
    } else {
      // requested url may be a substring of longest baseUrl
      // assume that this means that single unique service owns this base url
      val filtered = docs.filter { service ->
        url.startsWith(service.baseUrl)
      }
      val best = filtered.maxByOrNull { dd -> dd.baseUrl.length }

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
