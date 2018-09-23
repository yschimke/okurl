package com.baulsupp.okurl.credentials

import com.baulsupp.oksocial.output.process.exec
import com.baulsupp.oksocial.output.stdErrLogging
import pt.davidafsilva.apple.OSXKeychain
import pt.davidafsilva.apple.OSXKeychainException
import java.util.logging.Level
import java.util.logging.Logger

class OSXCredentialsStore : CredentialsStore {
  private val keychain: OSXKeychain = OSXKeychain.getInstance()

  override suspend fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? {
    val pw = keychain.findGenericPassword(serviceDefinition.apiHost(), tokenKey(tokenSet))

    val pwString = if (pw.isPresent) pw.get() else null

    if (pwString != null) {
      try {
        return serviceDefinition.parseCredentialsString(pwString)
      } catch (e: Exception) {
        logger.log(Level.FINE, "failed to parse", e)
      }
    }

    return null
  }

  override suspend fun <T> set(
    serviceDefinition: ServiceDefinition<T>,
    tokenSet: String,
    credentials: T
  ) {
    val credentialsString = serviceDefinition.formatCredentialsString(credentials)

    remove(serviceDefinition, tokenSet)

    try {
      keychain.addGenericPassword(serviceDefinition.apiHost(), tokenKey(tokenSet),
        credentialsString)
    } catch (e: OSXKeychainException) {
      logger.log(Level.WARNING, "Failed to write to keychain", e)
      throw RuntimeException(e)
    }
  }

  override suspend fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {
    try {
      keychain.deleteGenericPassword(serviceDefinition.apiHost(), tokenKey(tokenSet))
    } catch (e: OSXKeychainException) {
      logger.log(Level.FINE, "No key to delete", e)
    }
  }

  override suspend fun names(): Set<String> {
    val output = exec(listOf("security", "dump-keychain")) {
      redirectError(stdErrLogging)
      readOutput(true)
    }.outputString

    val names = output.lines().filter {
      it.matches(".*\"acct\".*\"oauth\\..*\".*".toRegex())
    }.map { it.replace(".*oauth\\.(\\w+).*".toRegex(), "$1") }

    return names.toSortedSet()
  }

  private fun tokenKey(tokenSet: String): String = "oauth.$tokenSet"

  companion object {
    private val logger = Logger.getLogger(OSXCredentialsStore::class.java.name)
  }
}
