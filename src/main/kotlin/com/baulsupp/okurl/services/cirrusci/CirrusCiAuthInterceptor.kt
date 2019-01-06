package com.baulsupp.okurl.services.cirrusci

import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition

class CirrusCiAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.cirrus-ci.com", "Cirrus CI", "cirrusci",
    null, null
  )
}
