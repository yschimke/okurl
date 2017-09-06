package com.baulsupp.oksocial.credentials

import com.google.common.base.Throwables
import com.mcdermottroe.apple.OSXKeychain
import com.mcdermottroe.apple.OSXKeychainException
import java.util.Optional
import java.util.logging.Level
import java.util.logging.Logger

class OSXCredentialsStore @Throws(OSXKeychainException::class)
@JvmOverloads constructor(private val tokenSet: Optional<String> = Optional.empty()) : CredentialsStore {
    private val keychain: OSXKeychain

    init {
        this.keychain = OSXKeychain.getInstance()
    }

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T> {
        try {
            val pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey())

            return Optional.ofNullable(serviceDefinition.parseCredentialsString(pw))
        } catch (e: OSXKeychainException) {
            if ("The specified item could not be found in the keychain." == e.message) {
                logger.log(Level.FINE,
                        "No OSX Keychain entry for '" + serviceDefinition.apiHost() + "' '" + tokenKey() + "'")
            } else {
                logger.log(Level.FINE, "Failed to read from keychain", e)
            }

            return Optional.empty()
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
        return "oauth" + tokenSet.map { s -> "." + s }.orElse("")
    }

    companion object {
        private val logger = Logger.getLogger(OSXCredentialsStore::class.java.name)
    }
}
