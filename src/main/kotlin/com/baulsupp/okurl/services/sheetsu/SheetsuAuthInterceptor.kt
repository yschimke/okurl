package com.baulsupp.okurl.services.sheetsu

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SheetsuAuthInterceptor : AuthInterceptor<BasicCredentials>() {
  override val serviceDefinition =
    BasicAuthServiceDefinition("sheetsu.com", "Sheetsu API", "sheetsu", "https://docs.sheetsu.com/", "https://sheetsu.com/dashboard")

  override suspend fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): BasicCredentials {
    val user =
      Secrets.prompt("Sheetsu API Key", "sheetsu.apiKey", "", false)
    val password =
      Secrets.prompt("Sheetsu API Password", "sheetsu.apiSecret", "", true)

    return BasicCredentials(user, password)
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("sheetsu.com")
}
