package com.baulsupp.okurl.services.transferwise

import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.ServiceDefinition

open class TransferwiseAuthInterceptor : BaseTransferwiseAuthInterceptor() {
  override val serviceDefinition: ServiceDefinition<Oauth2Token> = Oauth2ServiceDefinition(
    "api.transferwise.com", "Transferwise API", "transferwise",
    "https://api-docs.transferwise.com/docs/versions/v1/overview",
    "https://api-docs.transferwise.com/api-explorer/transferwise-api/versions/v1/"
  )
}
