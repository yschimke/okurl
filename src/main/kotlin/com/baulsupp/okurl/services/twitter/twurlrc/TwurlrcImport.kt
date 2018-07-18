package com.baulsupp.okurl.services.twitter.twurlrc

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.services.twitter.TwitterCredentials
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
