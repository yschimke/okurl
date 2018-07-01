package com.baulsupp.oksocial.services.howsmyssl

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.AbstractServiceDefinition
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://www.howsmyssl.com/
 */
class HowsMySslAuthInterceptor : AuthInterceptor<String>() {

  override fun intercept(chain: Interceptor.Chain, credentials: String): Response {
    var request = chain.request()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): String {
    TODO()
  }

  override val serviceDefinition =
    object: AbstractServiceDefinition<String>("www.howsmyssl.com", "Hows My SSL", "howsmyssl",
      "https://www.howsmyssl.com/s/api.html", null) {
      override fun parseCredentialsString(s: String): String = s

      override fun formatCredentialsString(credentials: String): String = credentials
    }

//  override suspend fun validate(
//    client: OkHttpClient,
//    credentials: String
//  ): ValidatedCredentials =
//    ValidatedCredentials(credentials, null)

  override fun hosts(): Set<String> = setOf((
    "www.howsmyssl.com")
  )
}
