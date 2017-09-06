package com.baulsupp.oksocial.credentials

import java.util.Optional

class FixedTokenCredentialsStore(private val token: String) : CredentialsStore {

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T> {
        return Optional.of(serviceDefinition.parseCredentialsString(token))
    }

    override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
        throw UnsupportedOperationException()
    }
}
