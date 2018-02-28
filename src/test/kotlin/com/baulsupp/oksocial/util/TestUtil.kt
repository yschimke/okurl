package com.baulsupp.oksocial.util

import com.baulsupp.oksocial.credentials.CredentialFactory
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.mcdermottroe.apple.OSXKeychainException
import org.junit.jupiter.api.Assumptions
import java.net.InetAddress
import java.net.UnknownHostException

object TestUtil {
  private var cachedException: UnknownHostException? = null
  private var initialised = false
  private var credentialsStore: CredentialsStore? = null

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

      try {
        credentialsStore = CredentialFactory.createCredentialsStore(null)
      } catch (e: OSXKeychainException) {
      }

      initialised = true
    }
  }

  @Synchronized
  fun assumeHasToken(
      serviceDefinition: ServiceDefinition<out Any>) {
    initialise()

    val token = credentialsStore!![serviceDefinition]

    Assumptions.assumeTrue(token != null)
  }
}
