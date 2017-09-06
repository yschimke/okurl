package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.output.OutputHandler
import java.io.IOException
import java.util.Comparator
import java.util.Optional
import java.util.concurrent.CompletableFuture
import okhttp3.OkHttpClient

import com.baulsupp.oksocial.output.util.FutureUtil.ioSafeGet
import com.baulsupp.oksocial.output.util.FutureUtil.join
import java.util.stream.Collectors.joining
import java.util.stream.Collectors.toList

class DiscoveryApiDocPresenter(private val discoveryIndex: DiscoveryIndex) : ApiDocPresenter {

    @Throws(IOException::class)
    override fun explainApi(url: String, outputHandler: OutputHandler<*>, client: OkHttpClient) {
        val discoveryPaths = discoveryIndex.getDiscoveryUrlForPrefix(url)

        val registry = DiscoveryRegistry.instance(client)

        val docs = join(discoveryPaths.stream().map { p -> registry.load(p) }.collect<List<CompletableFuture<DiscoveryDocument>>, Any>(toList()))

        val bestDoc = ioSafeGet(docs.thenApply { d ->
            val exactMatch = d.stream().filter { x -> matches(url, x) }.findFirst()

            if (exactMatch.isPresent) {
                return@docs.thenApply exactMatch
            }

            // requested url may be a substring of longest baseUrl
            // assume that this means that single unique service owns this base url
            val best = d.stream()
                    .filter { service -> url.startsWith(service.baseUrl) }
                    .max(Comparator.comparing<DiscoveryDocument, Int> { dd -> dd.baseUrl.length })

            if (best.isPresent) {
                return@docs.thenApply best
            }

            // multiple services sharing baseurl - return first
            outputHandler.info("Multiple services for path " + url)
            Optional.empty<DiscoveryDocument>()
        })

        if (bestDoc.isPresent) {
            val s = bestDoc.get()
            outputHandler.info("name: " + s.apiName)
            outputHandler.info("docs: " + s.docLink)

            val e = s.findEndpoint(url)

            e.ifPresent { de ->
                outputHandler.info("endpoint id: " + de.id())
                outputHandler.info("url: " + de.url())
                outputHandler.info("scopes: " + de.scopeNames().stream().collect<String, *>(joining(", ")))
                outputHandler.info("")
                outputHandler.info(de.description())
                outputHandler.info("")
                de.parameters().forEach { p ->
                    outputHandler.info("parameter: " + p.name() + " (" + p.type() + ")")
                    outputHandler.info(p.description())
                }
            }

            if (!e.isPresent) {
                outputHandler.info("base: " + s.baseUrl)
            }
        } else {
            outputHandler.info("No specific API found")
            outputHandler.info("https://developers.google.com/apis-explorer/#p/")
        }
    }

    private fun matches(url: String, x: DiscoveryDocument): Boolean {
        val eps = x.endpoints

        for (ep in eps) {
            if (ep.matches(url)) {
                return true
            }
        }

        return false
    }
}
