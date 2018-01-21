package com.baulsupp.oksocial.services.companieshouse

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.AbstractServiceDefinition
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CompaniesHouseAuthInterceptor : AuthInterceptor<String>() {

  override fun intercept(chain: Interceptor.Chain, credentials: String): Response {
    var request = chain.request()

    request = request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials, ""))
            .build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): String {
    val apiKey = Secrets.prompt("Companies House API Key", "companieshouse.apiKey", "", false)

    return apiKey
  }

  override fun serviceDefinition(): ServiceDefinition<String> =
    object: AbstractServiceDefinition<String>("api.companieshouse.gov.uk", "Companies House", "companieshouse",
                  "https://developer.companieshouse.gov.uk/api/docs/", "https://developer.companieshouse.gov.uk/developer/applications") {
      override fun parseCredentialsString(s: String): String = s

      override fun formatCredentialsString(credentials: String): String = credentials
    }

  suspend override fun validate(client: OkHttpClient,
                                credentials: String): ValidatedCredentials =
          ValidatedCredentials(credentials, null)

  override fun hosts(): Set<String> = setOf("api.companieshouse.gov.uk", "account.companieshouse.gov.uk", "document-api.companieshouse.gov.uk")
}
