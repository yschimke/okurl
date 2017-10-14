package com.baulsupp.oksocial.commands

import com.baulsupp.oksocial.completion.ArgumentCompleter
import okhttp3.OkHttpClient
import okhttp3.Request

interface ShellCommand {
    fun name(): String

    @Throws(Exception::class)
    fun buildRequests(client: OkHttpClient, requestBuilder: Request.Builder,
                      arguments: List<String>): List<Request>

    fun authenticator(): String? {
        return null
    }

    fun handlesRequests(): Boolean {
        return false
    }

    fun completer(): ArgumentCompleter? = null
}
