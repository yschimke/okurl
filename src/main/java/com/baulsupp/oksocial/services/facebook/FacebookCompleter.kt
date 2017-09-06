package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.logging.Level
import java.util.logging.Logger
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

import com.baulsupp.oksocial.services.facebook.FacebookUtil.VERSION
import com.google.common.collect.Lists.newArrayList
import java.util.stream.Collectors.joining
import java.util.stream.Collectors.toList
import java.util.stream.Stream.concat
import java.util.stream.Stream.of

class FacebookCompleter(private val client: OkHttpClient, hosts: Collection<String>) : HostUrlCompleter(hosts) {

    @Throws(IOException::class)
    override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
        var result = completePath(url.encodedPath())

        if (!url.encodedPath().endsWith("/")) {
            val parentPaths = url.encodedPathSegments()
            parentPaths.removeAt(parentPaths.size - 1)

            val parentPath = "/" + parentPaths.stream().collect<String, *>(joining("/"))

            result = result.thenCombine(completePath(parentPath), BiFunction<UrlList, UrlList, UrlList> { obj, b -> obj.combine(b) })
        }

        return result
    }

    private fun addPath(prefix: String): Function<String, String> {
        return { c -> prefix + (if (prefix.endsWith("/")) "" else "/") + c }
    }

    private fun topLevel(): CompletableFuture<List<String>> {
        val url = HttpUrl.parse(
                "https://graph.facebook.com/$VERSION/me/accounts?fields=username")
        val request = Request.Builder().url(url!!).build()

        return AuthUtil.enqueueJsonMapRequest(client, request)
                .thenApply { m ->
                    concat(
                            (m["data"] as List<Map<String, String>>).stream().map<String> { v -> v["username"] },
                            of("me")).collect<List<String>, Any>(toList())
                }
    }

    private fun completePath(path: String): CompletableFuture<UrlList> {
        if (path == "/") {
            return topLevel().thenApply { l ->
                l.add(VERSION)
                l
            }.thenApply { l ->
                UrlList(UrlList.Match.EXACT,
                        l.stream().map(addPath("https://graph.facebook.com/")).collect<List<String>, Any>(toList()))
            }
        } else if (path.matches("/v\\d.\\d/?".toRegex())) {
            return@topLevel ().thenApply topLevel ().thenApply(
                    { l ->
                        UrlList(UrlList.Match.EXACT,
                                l.stream().map(addPath("https://graph.facebook.com" + path)).collect(toList<String>()))
                    })
        } else {
            val prefix = "https://graph.facebook.com" + path

            val metadataFuture = FacebookUtil.getMetadata(client, HttpUrl.parse(prefix))

            return@topLevel ().thenApply metadataFuture . thenApply < UrlList >{ metadata ->
                val urls = metadata.connections().stream().map(addPath(prefix)).collect(toList<String>())
                urls.add(prefix)
                UrlList(UrlList.Match.EXACT, urls)
            }
                    .exceptionally { e ->
                logger.log(Level.FINE, "completion failure", e)
                UrlList(UrlList.Match.EXACT, newArrayList<String>())
            }
        }
    }

    companion object {
        private val logger = Logger.getLogger(FacebookCompleter::class.java.name)
    }
}
