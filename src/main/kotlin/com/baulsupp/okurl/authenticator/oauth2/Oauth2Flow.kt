package com.baulsupp.okurl.authenticator.oauth2

import com.baulsupp.okurl.authenticator.authflow.AuthFlow
import com.baulsupp.okurl.authenticator.authflow.AuthFlowType
import com.baulsupp.okurl.credentials.ServiceDefinition
import okhttp3.OkHttpClient

abstract class Oauth2Flow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) : AuthFlow<Oauth2Token> {
  override val type = AuthFlowType.Oauth2

  lateinit var client: OkHttpClient
  lateinit var state: String

  override suspend fun init(client: OkHttpClient, state: String) {
    this.client = client
    this.state = state
  }

  abstract suspend fun start(options: Map<String, Any>): String
  abstract suspend fun complete(code: String): Oauth2Token
}
