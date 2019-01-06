package com.baulsupp.okurl.services.howsmyssl

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.services.AbstractServiceDefinition

/**
 * https://www.howsmyssl.com/
 */
class HowsMySslAuthInterceptor : AuthInterceptor<String>() {
  override val serviceDefinition =
    object : AbstractServiceDefinition<String>(
      "www.howsmyssl.com", "Hows My SSL", "howsmyssl",
      "https://www.howsmyssl.com/s/api.html", null
    ) {
      override fun parseCredentialsString(s: String): String = s

      override fun formatCredentialsString(credentials: String): String = credentials
    }
}
