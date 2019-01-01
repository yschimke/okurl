package com.baulsupp.okurl.services.companieshouse

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CompaniesHouseAuthInterceptor : AuthInterceptor<String>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: String): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials, ""))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): String =
    Secrets.prompt("Companies House API Key", "companieshouse.apiKey", "", false)

  override val serviceDefinition = object : AbstractServiceDefinition<String>(
    "api.companieshouse.gov.uk",
    "Companies House",
    "companieshouse",
    "https://developer.companieshouse.gov.uk/api/docs/",
    "https://developer.companieshouse.gov.uk/developer/applications"
  ) {
    override fun parseCredentialsString(s: String): String = s

    override fun formatCredentialsString(credentials: String): String = credentials
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: String
  ): ValidatedCredentials =
    ValidatedCredentials(credentials, null)

  override fun hosts(credentialsStore: CredentialsStore): Set<String> =
    setOf("api.companieshouse.gov.uk", "account.companieshouse.gov.uk", "document-api.companieshouse.gov.uk")
}
