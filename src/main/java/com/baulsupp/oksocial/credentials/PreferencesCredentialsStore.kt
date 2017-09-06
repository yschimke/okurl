package com.baulsupp.oksocial.credentials

import java.util.Optional
import java.util.prefs.Preferences

class PreferencesCredentialsStore(private val tokenSet: Optional<String>) : CredentialsStore {
    private val userNode = Preferences.userNodeForPackage(this.javaClass)

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T> {
        val credentialsString = userNode.get(tokenKey(serviceDefinition.apiHost()), null)
        return Optional.ofNullable(credentialsString)
                .map(Function<String, T> { serviceDefinition.parseCredentialsString(it) })
    }

    private fun tokenKey(name: String): String {
        return name + ".token" + tokenSet.map { s -> "." + s }.orElse("")
    }

    override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
        val credentialsString = serviceDefinition.formatCredentialsString(credentials)
        userNode.put(tokenKey(serviceDefinition.apiHost()), credentialsString)
    }
}
