package com.baulsupp.oksocial

import com.baulsupp.oksocial.credentials.ServiceDefinition

abstract class AbstractServiceDefinition<T>(private val apiHost: String, private val serviceName: String, private val shortName: String,
                                            private val apiDocs: String, private val accountsLink: String?) : ServiceDefinition<T> {

    override fun shortName() = shortName

    override fun apiHost() = apiHost

    override fun serviceName() = serviceName

    override fun apiDocs() = apiDocs

    override fun accountsLink() = accountsLink
}
