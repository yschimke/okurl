package com.baulsupp.oksocial.services.smartystreets

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Response
import java.net.URLEncoder

object SmartyStreetsAuthFlow {
  suspend fun login(
    outputHandler: OutputHandler<Response>
  ): SmartStreetsToken {
    val authId = Secrets.prompt("SmartStreets Auth ID", "smartystreets.authId", "", false)
    val authToken = Secrets.prompt("SmartStreets Auth Token", "smartystreets.authToken", "", false)

    return SmartStreetsToken(authId, authToken)
  }
}
