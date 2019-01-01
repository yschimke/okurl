package com.baulsupp.okurl.services.cronhub

import com.baulsupp.okurl.services.AbstractServiceDefinition

object CronhubAuthServiceDefinition : AbstractServiceDefinition<CronhubCredentials>(
  "cronhub.io", "Cronhub API", "cronhub",
  "http://docs.cronhub.io/public-api.html", "https://cronhub.io/settings/api"
) {

  override fun parseCredentialsString(s: String): CronhubCredentials {
    return CronhubCredentials(s)
  }

  override fun formatCredentialsString(credentials: CronhubCredentials) =
    credentials.token
}
