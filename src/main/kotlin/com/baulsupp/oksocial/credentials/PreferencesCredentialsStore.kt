package com.baulsupp.oksocial.credentials

import java.util.prefs.Preferences

class PreferencesCredentialsStore : CredentialsStore {
  private val userNode = Preferences.userNodeForPackage(this.javaClass)

  override fun <T> get(serviceDefinition: ServiceDefinition<T>, tokenSet: String): T? {
    val credentialsString = userNode.get(tokenKey(serviceDefinition.apiHost(), tokenSet), null)
    return credentialsString?.let { serviceDefinition.parseCredentialsString(it) }
  }

  private fun tokenKey(name: String, tokenSet: String): String {
    return "$name.token.$tokenSet"
  }

  override fun <T> set(
    serviceDefinition: ServiceDefinition<T>, tokenSet: String, credentials: T) {
    val credentialsString = serviceDefinition.formatCredentialsString(credentials)
    userNode.put(tokenKey(serviceDefinition.apiHost(), tokenSet), credentialsString)
  }

  override fun <T> remove(serviceDefinition: ServiceDefinition<T>, tokenSet: String) {
    userNode.remove(tokenKey(serviceDefinition.apiHost(), tokenSet))
  }
}
