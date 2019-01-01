package com.baulsupp.okurl.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import org.conscrypt.OpenSSLProvider
import javax.net.ssl.SSLContext

@ExperimentalCoroutinesApi
private suspend fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

@ExperimentalCoroutinesApi
suspend fun main(args: Array<String>) {
  DebugProbes.install()

  SSLContext.getInstance("TLSv1.3", OpenSSLProvider())

  runMain("https://www.strava.com/api/v3/athlete/activities")
}
