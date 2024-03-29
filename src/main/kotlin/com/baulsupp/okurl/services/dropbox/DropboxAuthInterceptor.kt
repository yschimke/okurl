package com.baulsupp.okurl.services.dropbox

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.kotlin.requestBuilder
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * https://developer.dropbox.com/docs/authentication
 */
class DropboxAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.dropboxapi.com", "Dropbox API", "dropbox",
    "https://www.dropbox.com/developers/documentation/http/documentation", "https://www.dropbox.com/developers/apps"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val builder = request.newBuilder().addHeader("Authorization", "Bearer $token")

    if (request.method == "GET") {
      builder.method("POST", "{}".toRequestBody("application/json".toMediaType()))
    }

    request = builder.build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    val clientId = Secrets.prompt("Dropbox Client Id", "dropbox.clientId", "", false)
    val clientSecret = Secrets.prompt("Dropbox Client Secret", "dropbox.clientSecret", "", true)

    return DropboxAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val body = "null".toRequestBody("application/json".toMediaType())
    val request = requestBuilder(
      "https://api.dropboxapi.com/2/users/get_current_account",
      TokenValue(credentials)
    ).post(body).build()
    return ValidatedCredentials(client.queryMapValue<String>(request, "email"))
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> =
    setOf("api.dropboxapi.com", "content.dropboxapi.com")
}
