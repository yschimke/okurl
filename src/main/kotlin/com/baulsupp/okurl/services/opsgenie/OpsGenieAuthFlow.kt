package com.baulsupp.okurl.services.opsgenie

import com.baulsupp.okurl.authenticator.authflow.Prompt
import com.baulsupp.okurl.authenticator.authflow.PromptAuthFlow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition

class OpsGenieAuthFlow(override val serviceDefinition: ServiceDefinition<Oauth2Token>) :
  PromptAuthFlow<Oauth2Token>() {
  override fun options() = listOf(
    Prompt("opsgenie.apiKey", "OpsGenie API Key", null, false))

  override suspend fun complete(params: Map<String, Any>): Oauth2Token {
    return Oauth2Token(params["opsgenie.apiKey"] as String)
  }
}
