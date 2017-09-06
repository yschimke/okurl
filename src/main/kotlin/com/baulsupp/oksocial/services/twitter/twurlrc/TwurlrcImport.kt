package com.baulsupp.oksocial.services.twitter.twurlrc

import com.baulsupp.oksocial.services.twitter.TwitterCredentials
import com.baulsupp.oksocial.output.util.UsageException
import java.io.File
import java.util.Optional

object TwurlrcImport {
    fun authorize(authArguments: List<String>): TwitterCredentials {
        val twurlStore: TwurlCredentialsStore

        if (authArguments.size > 1) {
            twurlStore = TwurlCredentialsStore(File(authArguments[1]))
        } else {
            twurlStore = TwurlCredentialsStore.TWURL_STORE
        }

        val credentials = twurlStore.readCredentials()

        if (!credentials.isPresent) {
            throw UsageException("No credentials found in " + twurlStore.file)
        }

        return credentials.get()
    }
}
