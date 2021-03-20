package com.baulsupp.okurl.authenticator

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.authenticator.oauth2.Oauth2DesktopFlow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Flow
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.edit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

abstract class Oauth2AuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response =
    chain.proceed(chain.request().edit {
      addHeader("Authorization", "Bearer ${credentials.accessToken}")
    })

  override fun canRenew(credentials: Oauth2Token) = credentials.isRenewable()

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val authFlow = authFlow() as? Oauth2Flow ?: throw UsageException("Unsupported auth flow")

    return Oauth2DesktopFlow.login(authFlow, client, outputHandler)
  }
}
