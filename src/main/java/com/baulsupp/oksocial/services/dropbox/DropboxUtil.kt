package com.baulsupp.oksocial.services.dropbox

import com.google.common.collect.Sets
import java.util.Collections
import okhttp3.Request

object DropboxUtil {

    val API_HOSTS = Collections.unmodifiableSet(Sets.newHashSet("api.dropboxapi.com", "content.dropboxapi.com"))

    fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
        return requestBuilder.url("https://api.dropboxapi.com" + s).build()
    }
}
