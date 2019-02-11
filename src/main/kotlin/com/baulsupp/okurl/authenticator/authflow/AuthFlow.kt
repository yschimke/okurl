package com.baulsupp.okurl.authenticator.authflow

import com.baulsupp.okurl.credentials.ServiceDefinition
import okhttp3.OkHttpClient

interface AuthFlow<T> {
  val type: AuthFlowType
  val serviceDefinition: ServiceDefinition<T>

  suspend fun init(client: OkHttpClient)

  fun options(): List<AuthOption<*>>
}
