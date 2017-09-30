package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.google.common.collect.Lists
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.lang.Math.min
import java.time.Clock
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

class UrlCompleter(private val services: List<AuthInterceptor<*>>, private val client: OkHttpClient,
                   private val credentialsStore: CredentialsStore, private val completionVariableCache: CompletionVariableCache) : ArgumentCompleter {
    private val clock = Clock.systemDefaultZone()

    override fun urlList(prefix: String): UrlList {

        val fullUrl = parseUrl(prefix)

        if (fullUrl.isPresent) {
            val u = fullUrl.get()

            // won't match anything
            return services
                    .firstOrNull { it.supportsUrl(u) }
                    ?.let { it.apiCompleter(prefix, client, credentialsStore, completionVariableCache).siteUrls(u).get() }
                    ?: UrlList(UrlList.Match.EXACT, Lists.newArrayList())
        } else {
            val futures = Lists.newArrayList<Future<UrlList>>()

            services.mapTo(futures) { it.apiCompleter("", client, credentialsStore, completionVariableCache).prefixUrls() }

            return futuresToList(prefix, futures)
        }
    }

    private fun futuresToList(prefix: String, futures: List<Future<UrlList>>): UrlList {
        val to = clock.millis() + 2000

        val results = Lists.newArrayList<String>()

        for (f in futures) {
            try {
                val result = f.get(to - clock.millis(), TimeUnit.MILLISECONDS)

                results.addAll(result.getUrls(prefix))
            } catch (e: ExecutionException) {
                logger.log(Level.WARNING, "failure during url completion", e.cause)
            } catch (e: InterruptedException) {
                logger.log(Level.FINE, "timeout during url completion", e)
            } catch (e: TimeoutException) {
                logger.log(Level.FINE, "timeout during url completion", e)
            }

        }

        return UrlList(UrlList.Match.HOSTS, results)
    }

    private fun parseUrl(prefix: String): Optional<HttpUrl> {
        return if (isSingleApi(prefix)) {
            Optional.ofNullable(HttpUrl.parse(prefix))
        } else {
            Optional.empty()
        }
    }

    private fun isSingleApi(prefix: String): Boolean {
        return prefix.matches("https://[^/]+/.*".toRegex())
    }

    companion object {
        private val logger = Logger.getLogger(UrlCompleter::class.java.name)

        fun isPossibleAddress(urlCompletion: String): Boolean {
            return urlCompletion.startsWith("https://".substring(0, min(urlCompletion.length, 8)))
        }
    }
}
