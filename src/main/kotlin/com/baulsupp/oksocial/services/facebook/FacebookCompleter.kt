package com.baulsupp.oksocial.services.facebook

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.services.facebook.FacebookUtil.VERSION
import io.github.vjames19.futures.jdk8.map
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class FacebookCompleter(private val client: OkHttpClient, hosts: Collection<String>) : HostUrlCompleter(hosts) {

    @Throws(IOException::class)
    override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
        var result = completePath(url.encodedPath())

        if (!url.encodedPath().endsWith("/")) {
            val parentPaths = url.encodedPathSegments()
            parentPaths.removeAt(parentPaths.size - 1)

            val parentPath = "/" + parentPaths.joinToString("/")

            result = result.thenCombine(completePath(parentPath), { obj, b -> obj.combine(b) })
        }

        return result
    }

    private fun addPath(prefix: String): (String) -> String {
        return { c: String -> prefix + (if (prefix.endsWith("/")) "" else "/") + c }
    }

    private fun topLevel(): CompletableFuture<List<String>> {
        val url = HttpUrl.parse(
                "https://graph.facebook.com/$VERSION/me/accounts?fields=username")
        val request = Request.Builder().url(url!!).build()

        return AuthUtil.enqueueJsonMapRequest(client, request)
                .map { m ->
                    (m["data"] as List<Map<String, String>>).map { it["username"]!! } + "me"
                }.exceptionally { mutableListOf() }
    }

    private fun completePath(path: String): CompletableFuture<UrlList> {
        when {
            path == "/" -> return topLevel().map { it + VERSION }.thenApply { l -> UrlList(UrlList.Match.EXACT, l.map(addPath("https://graph.facebook.com/"))) }
            path.matches("/v\\d.\\d+/?".toRegex()) -> return topLevel().map { l -> UrlList(UrlList.Match.EXACT, l.map(addPath("https://graph.facebook.com" + path))) }
            else -> {
                val prefix = "https://graph.facebook.com" + path

                val metadataFuture = FacebookUtil.getMetadata(client, HttpUrl.parse(prefix)!!)

                return metadataFuture.map { metadata ->
                    UrlList(UrlList.Match.EXACT, metadata.connections().map(addPath(prefix)) + prefix)
                }.exceptionally { e ->
                    logger.log(Level.FINE, "completion failure", e)
                    UrlList(UrlList.Match.EXACT, listOf())
                }
            }
        }
    }

    companion object {
        private val logger = Logger.getLogger(FacebookCompleter::class.java.name)
    }
}
