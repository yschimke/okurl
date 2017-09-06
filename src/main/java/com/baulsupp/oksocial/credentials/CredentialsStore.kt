package com.baulsupp.oksocial.credentials

import java.util.Optional

import java.util.Optional.empty

interface CredentialsStore {

    fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T>

    fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>)

    companion object {
        val NONE: CredentialsStore = object : CredentialsStore {
            override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T> {
                return empty()
            }

            override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {}
        }
    }
}
