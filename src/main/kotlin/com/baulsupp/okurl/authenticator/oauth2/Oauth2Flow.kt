package com.baulsupp.okurl.authenticator.oauth2

import com.baulsupp.okurl.authenticator.authflow.AuthFlow
import com.baulsupp.okurl.authenticator.authflow.AuthFlowType
import com.baulsupp.okurl.credentials.ServiceDefinition
import okhttp3.OkHttpClient

abstract class Oauth2Flow<T>(override val serviceDefinition: ServiceDefinition<T>) : AuthFlow<T> {
  override val type = AuthFlowType.Oauth2

  lateinit var client: OkHttpClient
  val options = mutableMapOf<String, Any>()

  override suspend fun init(client: OkHttpClient) {
    this.client = client
  }

  fun defineOptions(options: Map<String, Any>) {
    this.options.putAll(options)
  }

  abstract suspend fun start(): String
  abstract suspend fun complete(code: String): Oauth2Token
}
