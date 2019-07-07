package com.baulsupp.okurl.services.twitter

import okhttp3.Interceptor
import okhttp3.Response

class TwitterCachingInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalResponse = chain.proceed(chain.request())

    val host = chain.request().url.host

    if (TwitterUtil.TWITTER_API_HOSTS.contains(host)) {
      if (originalResponse.code == 200) {
        var cacheSeconds = 60

        if (permanentHosts.contains(host)) {
          cacheSeconds = 3600
        }

        return originalResponse.newBuilder()
          .header("Cache-Control", "max-age=$cacheSeconds")
          .removeHeader("expires")
          .removeHeader("pragma")
          .build()
      }
    }

    return originalResponse
  }

  companion object {
    private val permanentHosts = setOf("pbs.twimg.com")
  }
}
