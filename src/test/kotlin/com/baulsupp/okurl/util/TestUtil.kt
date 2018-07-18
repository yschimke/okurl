package com.baulsupp.okurl.util

import com.baulsupp.okurl.credentials.CredentialFactory
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.DefaultToken
import com.baulsupp.okurl.credentials.ServiceDefinition
import org.junit.jupiter.api.Assumptions
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException

object TestUtil {
  private var cachedException: UnknownHostException? = null
  private var initialised = false
  lateinit var credentialsStore: CredentialsStore

  @Synchronized
  fun assumeHasNetwork() {
    initialise()

    Assumptions.assumeTrue(cachedException == null)
  }

  private fun initialise() {
    if (!initialised) {
      try {
        InetAddress.getByName("www.google.com")
      } catch (e: UnknownHostException) {
        cachedException = e
      }

      if (!this::credentialsStore.isInitialized) {
        credentialsStore = CredentialFactory.createCredentialsStore()
      }

      initialised = true
    }
  }

  @Synchronized
  fun assumeHasToken(
    serviceDefinition: ServiceDefinition<out Any>
  ) {
    initialise()

    val token = credentialsStore.get(serviceDefinition,
      DefaultToken)

    Assumptions.assumeTrue(token != null)
  }

  fun projectFile(s: String): File {

    return File(projectRoot(), s)
  }

  val projectRoot = {
    var root = File(TestUtil::class.java.getResource("/datasettes.txt").file)

    while (!File(root, ".git").exists()) {
      root = root.parentFile
    }

    root
  }
}
