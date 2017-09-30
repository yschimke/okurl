package com.baulsupp.oksocial.credentials

import java.util.prefs.Preferences

class PreferencesCredentialsStore(private val tokenSet: String?) : CredentialsStore {
    private val userNode = Preferences.userNodeForPackage(this.javaClass)

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): T? {
        val credentialsString = userNode.get(tokenKey(serviceDefinition.apiHost()), null)
        return credentialsString?.let { serviceDefinition.parseCredentialsString(it) }
    }

    private fun tokenKey(name: String): String {
        return "$name.token${tokenSet?.let { "." + tokenSet }.orEmpty()}"
    }

    override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
        val credentialsString = serviceDefinition.formatCredentialsString(credentials)
        userNode.put(tokenKey(serviceDefinition.apiHost()), credentialsString)
    }
}
