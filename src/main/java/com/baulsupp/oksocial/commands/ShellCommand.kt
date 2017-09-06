package com.baulsupp.oksocial.commands

import com.baulsupp.oksocial.completion.ArgumentCompleter
import java.util.Optional
import okhttp3.OkHttpClient
import okhttp3.Request

import java.util.Optional.empty

interface ShellCommand {
    fun name(): String

    @Throws(Exception::class)
    fun buildRequests(client: OkHttpClient, requestBuilder: Request.Builder,
                      arguments: List<String>): List<Request>

    open fun authenticator(): Optional<String> {
        return empty()
    }

    open fun handlesRequests(): Boolean {
        return false
    }

    fun completer(): Optional<ArgumentCompleter> {
        return empty()
    }
}
