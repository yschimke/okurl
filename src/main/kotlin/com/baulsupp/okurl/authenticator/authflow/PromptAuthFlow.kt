package com.baulsupp.okurl.authenticator.authflow

import okhttp3.OkHttpClient

abstract class PromptAuthFlow<T>: AuthFlow<T> {
  override val type = AuthFlowType.Prompt

  override suspend fun init(client: OkHttpClient) {
  }

  abstract suspend fun complete(params: Map<String, Any>): T
}
