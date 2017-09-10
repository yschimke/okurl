package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.google.common.collect.Maps
import java.util.*
import java.util.Optional.ofNullable

class TestCredentialsStore : CredentialsStore {
    var tokens: MutableMap<String, String> = Maps.newHashMap()

    override fun <T> readDefaultCredentials(serviceDefinition: ServiceDefinition<T>): Optional<T> {
        return ofNullable(tokens[serviceDefinition.apiHost()])
                .map({ serviceDefinition.parseCredentialsString(it) })
    }

    override fun <T> storeCredentials(credentials: T, serviceDefinition: ServiceDefinition<T>) {
        tokens.put(serviceDefinition.apiHost(), serviceDefinition.formatCredentialsString(credentials))
    }
}
