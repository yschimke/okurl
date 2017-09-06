package com.baulsupp.oksocial.services.github

import com.google.common.collect.Sets
import java.util.Arrays
import java.util.Collections
import okhttp3.Request

object GithubUtil {
    val SCOPES: Collection<String> = Arrays.asList(
            "user",
            "repo",
            "gist",
            "admin:org")

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet(
            "api.github.com", "uploads.github.com")
    )

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.github.com" + s).build()
    }
}
