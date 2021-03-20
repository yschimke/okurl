package com.baulsupp.okurl.commands

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenSet
import com.baulsupp.okurl.location.LocationSource
import com.baulsupp.okurl.services.ServiceLibrary
import okhttp3.OkHttpClient
import okhttp3.Response

interface ToolSession {
  fun close()

  var client: OkHttpClient
  var outputHandler: OutputHandler<Response>
  var credentialsStore: CredentialsStore
  var locationSource: LocationSource
  var serviceLibrary: ServiceLibrary
  val defaultTokenSet: TokenSet?
}
