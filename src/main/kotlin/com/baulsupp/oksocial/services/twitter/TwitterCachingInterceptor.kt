package com.baulsupp.oksocial.services.twitter

import com.google.common.collect.Sets
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class TwitterCachingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        val host = chain.request().url().host()

        if (TwitterUtil.TWITTER_API_HOSTS.contains(host)) {
            if (originalResponse.code() == 200) {
                var cacheSeconds = 60

                if (permanentHosts.contains(host)) {
                    cacheSeconds = 3600
                }

                return originalResponse.newBuilder()
                        .header("Cache-Control", "max-age=" + cacheSeconds)
                        .removeHeader("expires")
                        .removeHeader("pragma")
                        .build()
            }
        }

        return originalResponse
    }

    companion object {
        private val permanentHosts = Sets.newHashSet("pbs.twimg.com")
    }
}
