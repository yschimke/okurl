package com.baulsupp.oksocial.util

import com.baulsupp.oksocial.credentials.CredentialFactory
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.mcdermottroe.apple.OSXKeychainException
import org.junit.Assume


import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Optional.empty

object TestUtil {
    private var cachedException: UnknownHostException? = null
    private var initialised = false
    private var credentialsStore: CredentialsStore? = null

    @Synchronized
    fun assumeHasNetwork() {
        initialise()

        Assume.assumeTrue(cachedException == null)
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

        val token = credentialsStore!!.readDefaultCredentials(serviceDefinition)

        Assume.assumeTrue(token != null)
    }
}
