package com.baulsupp.okurl.services.smartystreets

import com.baulsupp.okurl.secrets.Secrets

object SmartyStreetsAuthFlow {
  suspend fun login(): SmartStreetsToken {
    val authId = Secrets.prompt("SmartStreets Auth ID", "smartystreets.authId", "", false)
    val authToken = Secrets.prompt("SmartStreets Auth Token", "smartystreets.authToken", "", false)

    return SmartStreetsToken(authId, authToken)
  }
}
