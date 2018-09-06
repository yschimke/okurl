package com.baulsupp.okurl.services

import com.baulsupp.okurl.credentials.ServiceDefinition

abstract class AbstractServiceDefinition<T>(
  private val apiHost: String,
  private val serviceName: String,
  private val shortName: String,
  private val apiDocs: String? = null,
  private val accountsLink: String? = null
) : ServiceDefinition<T> {

  override fun shortName() = shortName

  override fun apiHost() = apiHost

  override fun serviceName() = serviceName

  override fun apiDocs() = apiDocs

  override fun accountsLink() = accountsLink
}
