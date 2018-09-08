package com.baulsupp.okurl.services.smartystreets

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SmartyStreetsAuthInterceptor : AuthInterceptor<SmartStreetsToken>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: SmartStreetsToken): Response {
    var request = chain.request()

    val signedUrl = request.url().newBuilder().addQueryParameter("auth-id", credentials.authId).addQueryParameter("auth-token", credentials.authToken).build()

    request = request.newBuilder().url(signedUrl).build()

    return chain.proceed(request)
  }

  override val serviceDefinition = object :
    AbstractServiceDefinition<SmartStreetsToken>("api.smartystreets.com", "SmartyStreets", "smartystreets",
      "https://smartystreets.com/docs/cloud", "https://smartystreets.com/account") {
    override fun parseCredentialsString(s: String): SmartStreetsToken {
      val (token, key) = s.split(":", limit = 2)
      return SmartStreetsToken(token, key)
    }

    override fun formatCredentialsString(credentials: SmartStreetsToken): String =
      credentials.authId + ":" + credentials.authToken
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): SmartStreetsToken {
    return SmartyStreetsAuthFlow.login()
  }

  override suspend fun supportsUrl(url: HttpUrl, credentialsStore: CredentialsStore): Boolean {
    return url.host() == "api.smartystreets.com" || url.host().endsWith(".api.smartystreets.com")
  }

  override fun hosts(): Set<String> = setOf("international-street.api.smartystreets.com", "us-street.api.smartystreets.com", "us-zipcode.api.smartystreets.com", "us-autocomplete.api.smartystreets.com", "us-extract.api.smartystreets.com", "download.api.smartystreets.com")
}
