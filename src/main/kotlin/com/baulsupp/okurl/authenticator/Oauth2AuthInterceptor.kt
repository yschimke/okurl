package com.baulsupp.okurl.authenticator

import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.edit
import okhttp3.Interceptor
import okhttp3.Response

abstract class Oauth2AuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response =
    chain.proceed(chain.request().edit {
      addHeader("Authorization", "Bearer ${credentials.accessToken}")
    })

  override fun canRenew(credentials: Oauth2Token) = credentials.isRenewable()
}
