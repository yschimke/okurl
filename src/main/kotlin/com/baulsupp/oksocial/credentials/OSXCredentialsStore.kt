package com.baulsupp.oksocial.credentials

import com.google.common.base.Throwables
import com.mcdermottroe.apple.OSXKeychain
import com.mcdermottroe.apple.OSXKeychainException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class OSXCredentialsStore @Throws(OSXKeychainException::class)
@JvmOverloads constructor(private val tokenSet: String? = null) : CredentialsStore {
    private val keychain: OSXKeychain

    init {
        this.keychain = OSXKeychain.getInstance()
    }

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
        try {
            val pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey())

            return serviceDefinition.parseCredentialsString(pw)
        } catch (e: OSXKeychainException) {
            if ("The specified item could not be found in the keychain." == e.message) {
                logger.log(Level.FINE,
                        "No OSX Keychain entry for '" + serviceDefinition.apiHost() + "' '" + tokenKey() + "'")
            } else {
                logger.log(Level.FINE, "Failed to read from keychain", e)
            }

            return null
        }

    }

    override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
        val credentialsString = serviceDefinition.formatCredentialsString(credentials)

        try {
            keychain.deleteGenericPassword(serviceDefinition.apiHost(), tokenKey())
        } catch (e: OSXKeychainException) {
            logger.log(Level.FINE, "No key to delete", e)
        }

        try {
            keychain.addGenericPassword(serviceDefinition.apiHost(), tokenKey(), credentialsString)
        } catch (e: OSXKeychainException) {
            logger.log(Level.WARNING, "Failed to write to keychain", e)
            throw Throwables.propagate(e)
        }

    }

    private fun tokenKey(): String {
        return "oauth${tokenSet?.let { "." + it } ?: ""}"
    }

    companion object {
        private val logger = Logger.getLogger(OSXCredentialsStore::class.java.name)
    }
}
