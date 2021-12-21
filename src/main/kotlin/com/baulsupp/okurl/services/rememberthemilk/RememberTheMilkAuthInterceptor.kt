package com.baulsupp.okurl.services.rememberthemilk

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.AbstractServiceDefinition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.ByteString.Companion.encodeUtf8

class RememberTheMilkAuthInterceptor : AuthInterceptor<RememberTheMilkCredentials>() {
  override val serviceDefinition = object : AbstractServiceDefinition<RememberTheMilkCredentials>(
    "api.rememberthemilk.com", "RememberTheMilk API", "rtm",
    "https://www.rememberthemilk.com/services/api/overview.rtm", "https://www.rememberthemilk.com/services/api/"
  ) {
    override fun parseCredentialsString(s: String): RememberTheMilkCredentials {
      val parts = s.split(":")
      return RememberTheMilkCredentials(parts[0], parts[1], parts.getOrNull(2))
    }

    override fun formatCredentialsString(credentials: RememberTheMilkCredentials): String =
      "${credentials.api_key}:${credentials.api_secret}:${credentials.token.orEmpty()}"
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> {
    return setOf("api.rememberthemilk.com")
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: RememberTheMilkCredentials): Response {
    var request = chain.request()

    val unsignedUrl = request.url.newBuilder()
      .addQueryParameter("api_key", credentials.api_key)
      .apply {
        if (credentials.token != null) {
          addQueryParameter("auth_token", credentials.token)
        }
      }
      .build()

    val sig =
      credentials.api_secret + unsignedUrl.queryParameterNames.sorted().map { it + unsignedUrl.queryParameter(it) }
        .joinToString("")

    val signedUrl = unsignedUrl.newBuilder().addQueryParameter("api_sig", sig.encodeUtf8().md5().hex()).build()

    request = request.newBuilder()
      .url(signedUrl)
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): RememberTheMilkCredentials {
    val apiKey = Secrets.prompt("RTM API Key", "rtm.apiKey", "", false)
    val apiSecret = Secrets.prompt("RTM API Secret", "rtm.apiSecret", "", false)

    return RememberTheMilkAuthFlow.login(client, outputHandler, apiKey, apiSecret)
  }
}
