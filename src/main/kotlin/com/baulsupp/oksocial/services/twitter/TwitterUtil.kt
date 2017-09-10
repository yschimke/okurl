package com.baulsupp.oksocial.services.twitter

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object TwitterUtil {

    val TWITTER_API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.twitter.com", "upload.twitter.com", "stream.twitter.com", "mobile.twitter.com",
            "syndication.twitter.com", "pbs.twimg.com", "t.co", "userstream.twitter.com",
            "sitestream.twitter.com", "search.twitter.com")
    )

    val TWITTER_WEB_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "www.twitter.com", "twitter.com")
    )

    val TWITTER_HOSTS = Collections.unmodifiableSet(Sets.union(TWITTER_API_HOSTS, TWITTER_WEB_HOSTS))

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.twitter.com" + s).build()
    }
}
