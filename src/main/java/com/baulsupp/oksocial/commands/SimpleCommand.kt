package com.baulsupp.oksocial.commands

import com.baulsupp.oksocial.output.util.UsageException
import java.util.Optional
import java.util.stream.Collectors
import okhttp3.OkHttpClient
import okhttp3.Request

open class SimpleCommand(private val name: String, private val prefix: String, private val authName: String) : ShellCommand {

    override fun name(): String {
        return name
    }

    fun mapUrl(url: String): String {
        return prefix + url
    }

    override fun buildRequests(clientBuilder: OkHttpClient,
                               requestBuilder: Request.Builder, urls: List<String>): List<Request> {
        try {
            return urls.stream().map { u -> requestBuilder.url(mapUrl(u)).build() }.collect<List<Request>, Any>(
                    Collectors.toList())
        } catch (iae: IllegalArgumentException) {
            throw UsageException(iae.message)
        }

    }

    override fun authenticator(): Optional<String> {
        return Optional.ofNullable(authName)
    }
}
