package com.baulsupp.oksocial.services.oxforddictionaries;

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.kotlin.edit
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.AbstractServiceDefinition
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.Arrays

/**
 * https://developer.lyft.com/docs/authentication
 */
class OxfordDictionariesInterceptor : AuthInterceptor<ODToken>() {
  override fun serviceDefinition(): ServiceDefinition<ODToken> {
    return object : AbstractServiceDefinition<ODToken>("od-api.oxforddictionaries.com", "Oxford Dictionaries API", "oxforddictionaries",
      "https://developer.oxforddictionaries.com/documentation", "https://developer.oxforddictionaries.com/admin/applications") {
      override fun parseCredentialsString(s: String): ODToken {
        val (id, key) = s.split(":", limit = 2)
        return ODToken(id, key)
      }

      override fun formatCredentialsString(credentials: ODToken): String {
        return credentials.appId + ":" + credentials.appKey
      }
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

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): ODToken {
    outputHandler.openLink("https://developer.oxforddictionaries.com/credentials")

    val appId = Secrets.prompt("OD App Id", "od.appId", "", false)
    val appKey = Secrets.prompt("OD App Key", "od.appKey", "", true)

    return ODToken(appId, appKey)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: ODToken): ValidatedCredentials =
    ValidatedCredentials(null, null)

  override fun hosts(): Set<String> = setOf("od-api.oxforddictionaries.com")
}
