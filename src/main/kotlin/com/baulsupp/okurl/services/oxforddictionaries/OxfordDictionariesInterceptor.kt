package com.baulsupp.okurl.services.oxforddictionaries

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.kotlin.edit
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class OxfordDictionariesInterceptor : AuthInterceptor<ODToken>() {
  override val serviceDefinition = object : AbstractServiceDefinition<ODToken>("od-api.oxforddictionaries.com", "Oxford Dictionaries API", "oxforddictionaries",
    "https://developer.oxforddictionaries.com/documentation", "https://developer.oxforddictionaries.com/admin/applications") {
    override fun parseCredentialsString(s: String): ODToken {
      val (id, key) = s.split(":", limit = 2)
      return ODToken(id, key)
    }

    override fun formatCredentialsString(credentials: ODToken): String {
      return credentials.appId + ":" + credentials.appKey
    }
  }

  override fun intercept(chain: Interceptor.Chain, credentials: ODToken): Response {
    var request = chain.request()

    request = request.edit {
      if (request.header("Accept") == null) {
        addHeader("Accept", "application/json")
      }
      addHeader("app_id", credentials.appId)
      addHeader("app_key", credentials.appKey)
    }

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): ODToken {
    outputHandler.openLink("https://developer.oxforddictionaries.com/credentials")

    val appId = Secrets.prompt("OD App Id", "od.appId", "", false)
    val appKey = Secrets.prompt("OD App Key", "od.appKey", "", true)

    return ODToken(appId, appKey)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: ODToken
  ): ValidatedCredentials =
    ValidatedCredentials(null, null)

  override fun hosts(): Set<String> = setOf("od-api.oxforddictionaries.com")
}
