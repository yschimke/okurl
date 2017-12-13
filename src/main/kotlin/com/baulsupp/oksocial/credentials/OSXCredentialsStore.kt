package com.baulsupp.oksocial.credentials

import com.mcdermottroe.apple.OSXKeychain
import com.mcdermottroe.apple.OSXKeychainException
import java.util.logging.Level
import java.util.logging.Logger

class OSXCredentialsStore(private val tokenSet: String? = null) : CredentialsStore {
  private val keychain: OSXKeychain = OSXKeychain.getInstance()

  override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
    return try {
      val pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey())

      serviceDefinition.parseCredentialsString(pw)
    } catch (e: OSXKeychainException) {
      if ("The specified item could not be found in the keychain." == e.message) {
        logger.log(Level.FINE,
                "No OSX Keychain entry for '" + serviceDefinition.apiHost() + "' '" + tokenKey() + "'")
      } else {
        logger.log(Level.FINE, "Failed to read from keychain", e)
      }

      null
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
      throw RuntimeException(e)
    }
  }

  private fun tokenKey(): String = "oauth${tokenSet?.let { "." + it } ?: ""}"

  companion object {
    private val logger = Logger.getLogger(OSXCredentialsStore::class.java.name)

    fun isAvailable(): Boolean = try {
      Class.forName("com.mcdermottroe.apple.OSXKeychain")
      true
    } catch (e: Exception) {
      false
    }
  }
}
