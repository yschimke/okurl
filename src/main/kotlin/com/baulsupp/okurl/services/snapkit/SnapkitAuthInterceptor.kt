package com.baulsupp.okurl.services.snapkit

import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.services.github.GithubAuthFlow
import com.baulsupp.okurl.services.strava.model.Athlete
import com.baulsupp.okurl.services.strava.model.AuthResponse
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.logging.Level

class SnapkitAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "snapchat.com", "Snapkit", "snapkit", "https://kit.snapchat.com/docs/",
    "https://kit.snapchat.com/manage/"
  )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    return setOf("bitmoji.api.snapchat.com", "sdk.bitmoji.com")
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    return chain.proceed(chain.request().edit {
      addHeader("Authorization", "Bearer ${credentials.accessToken}")
    })
  }

  // scopes
  // https://auth.snapchat.com/oauth2/api/user.display_name
  //   user.bitmoji.avatar

  override fun authFlow() = SnapkitAuthFlow(serviceDefinition)

  // TODO validate
  // TODO renew
}
