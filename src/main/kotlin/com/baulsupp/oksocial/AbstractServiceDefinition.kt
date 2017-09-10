package com.baulsupp.oksocial

import com.baulsupp.oksocial.credentials.ServiceDefinition
import java.util.*
import java.util.Optional.ofNullable

abstract class AbstractServiceDefinition<T>(private val apiHost: String, private val serviceName: String, private val shortName: String,
                                            private val apiDocs: String, private val accountsLink: String) : ServiceDefinition<T> {

    override fun shortName(): String {
        return shortName
    }

    override fun apiHost(): String {
        return apiHost
    }

    override fun serviceName(): String {
        return serviceName
    }

    override fun apiDocs(): Optional<String> {
        return ofNullable(apiDocs)
    }

    override fun accountsLink(): Optional<String> {
        return ofNullable(accountsLink)
    }
}
