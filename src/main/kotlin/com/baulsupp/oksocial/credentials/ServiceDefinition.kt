package com.baulsupp.oksocial.credentials

import java.util.*
import java.util.Optional.empty

interface ServiceDefinition<T> {
    fun apiHost(): String

    fun serviceName(): String

    fun parseCredentialsString(s: String): T

    fun formatCredentialsString(credentials: T): String

    fun shortName(): String

    fun apiDocs(): Optional<String> {
        return empty()
    }

    fun accountsLink(): Optional<String> {
        return empty()
    }
}
