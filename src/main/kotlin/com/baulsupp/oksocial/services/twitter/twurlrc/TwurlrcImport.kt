package com.baulsupp.oksocial.services.twitter.twurlrc

import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.services.twitter.TwitterCredentials
import java.io.File

object TwurlrcImport {
  fun authorize(authArguments: List<String>): TwitterCredentials {
    val twurlStore: TwurlCredentialsStore = if (authArguments.size > 1) {
      TwurlCredentialsStore(File(authArguments[1]))
    } else {
      TwurlCredentialsStore.TWURL_STORE
    }

    return twurlStore.readCredentials() ?: throw UsageException(
            "No credentials found in " + twurlStore.file)
  }
}
