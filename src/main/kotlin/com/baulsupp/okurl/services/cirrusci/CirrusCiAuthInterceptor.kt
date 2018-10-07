package com.baulsupp.okurl.services.cirrusci

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.credentials.CredentialsStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CirrusCiAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response = chain.proceed(chain.request())

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): Oauth2Token =
    TODO()

  override val serviceDefinition = Oauth2ServiceDefinition("api.cirrus-ci.com", "Cirrus CI", "cirrusci",
    null, null)

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.cirrus-ci.com")
}
