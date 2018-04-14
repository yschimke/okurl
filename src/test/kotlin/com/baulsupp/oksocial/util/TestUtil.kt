package com.baulsupp.oksocial.util

import com.baulsupp.oksocial.credentials.DefaultToken
import com.baulsupp.oksocial.credentials.CredentialFactory
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import org.junit.jupiter.api.Assumptions
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
}
