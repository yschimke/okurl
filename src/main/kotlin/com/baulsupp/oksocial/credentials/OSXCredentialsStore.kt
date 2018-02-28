package com.baulsupp.oksocial.credentials

import pt.davidafsilva.apple.OSXKeychain
import pt.davidafsilva.apple.OSXKeychainException
import java.util.logging.Level
import java.util.logging.Logger

class OSXCredentialsStore(private val tokenSet: String? = null) : CredentialsStore {
  private val keychain: OSXKeychain = OSXKeychain.getInstance()

  override fun <T> get(serviceDefinition: ServiceDefinition<T>): T? {
    val pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey())

    return pw.map { serviceDefinition.parseCredentialsString(it) }.orElse(null)
  }

  override fun <T> set(
    serviceDefinition: ServiceDefinition<T>, credentials: T) {
    val credentialsString = serviceDefinition.formatCredentialsString(credentials)

    remove(serviceDefinition)

    try {
      keychain.addGenericPassword(serviceDefinition.apiHost(), tokenKey(), credentialsString)
    } catch (e: OSXKeychainException) {
      logger.log(Level.WARNING, "Failed to write to keychain", e)
      throw RuntimeException(e)
    }
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>) {
    try {
      keychain.deleteGenericPassword(serviceDefinition.apiHost(), tokenKey())
    } catch (e: OSXKeychainException) {
      logger.log(Level.FINE, "No key to delete", e)
    }
  }

  private fun tokenKey(): String = "oauth${tokenSet?.let { "." + it } ?: ""}"

  companion object {
    private val logger = Logger.getLogger(OSXCredentialsStore::class.java.name)

    fun isAvailable(): Boolean = try {
      Class.forName("pt.davidafsilva.apple.OSXKeychain")
      true
    } catch (e: Exception) {
      false
    }
  }
}
