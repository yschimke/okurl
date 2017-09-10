package com.baulsupp.oksocial.services.github

import com.google.common.collect.Sets
import okhttp3.Request
import java.util.*

object GithubUtil {
    val SCOPES = listOf("user","repo","gist","admin:org")

    val API_HOSTS = listOf("api.github.com", "uploads.github.com")

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.github.com" + s).build()
    }
}
